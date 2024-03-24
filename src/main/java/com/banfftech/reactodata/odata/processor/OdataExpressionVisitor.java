package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.Util;
import com.banfftech.reactodata.service.SqlHolder;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.expression.*;

import java.util.*;

// OdataExpressionVisitor will translate odata FilterOption to hibernate query
public class OdataExpressionVisitor implements ExpressionVisitor {
    public final static Map<BinaryOperatorKind, String> COMPARISONOPERATORMAP = new HashMap<>();

    static {
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.EQ, "=");
        // NOT EMPTY
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.NE, "!=");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.GE, ">=");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.GT, ">");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.LE, "<=");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.LT, "<");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.HAS, "like");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.IN, "in");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.AND, "and");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.OR, "or");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.ADD, "+");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.SUB, "-");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.MUL, "*");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.DIV, "/");
        COMPARISONOPERATORMAP.put(BinaryOperatorKind.MOD, "%");
    }
    private final EdmEntityType edmEntityType;
    private final String mainTableName;
    private EdmEntityType joinEdmEntityType;
    private String joinAlias;
    private Map<String, String> tableAlias = new HashMap<>();
    private String joinSql;
    private String groupBySql;
    private SqlHolder sqlHolder;
    private boolean needGroupBy = false;

    public OdataExpressionVisitor(EdmEntityType edmEntityType) {
        this.edmEntityType = edmEntityType;
        this.mainTableName = Util.javaNameToDbName(edmEntityType.getName());
        this.joinSql = "";
        this.sqlHolder = new SqlHolder(mainTableName);
        this.joinEdmEntityType = edmEntityType;
        this.joinAlias = mainTableName;
        this.groupBySql = "";
    }

    @Override
    public Object visitBinaryOperator(BinaryOperatorKind binaryOperatorKind, Object o, Object t1) throws ExpressionVisitException, ODataApplicationException {
        Object l = o;
        if (o instanceof EdmProperty) {
            String propertyName = ((EdmProperty) o).getName();
            l = Util.javaNameToDbName(propertyName);
        }
        String result = l + " " + COMPARISONOPERATORMAP.get(binaryOperatorKind) + " " + t1;
        return result;
    }

    @Override
    public Object visitUnaryOperator(UnaryOperatorKind unaryOperatorKind, Object o) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitMethodCall(MethodKind methodKind, List list) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitLambdaExpression(String s, String s1, Expression expression) throws ExpressionVisitException, ODataApplicationException {
        return expression.accept(this);
    }

    @Override
    public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
        return literal.getText();
    }

    @Override
    public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
        final List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();

        // 例如：Products?$filter=ProductFeatureAppl/any(c:c/productFeatureId eq 'SIZE_2' or c/productFeatureId eq 'SIZE_6')
        // uriResourceParts[0] 是ProductFeatureAppl
        // uriResourceParts[1] 是UriResourceLambdaAnyImpl，其lambdaVariable是c
        // 然后会再次进入这个方法，uriResourceParts[0]是UriResourceLambdaVarImpl，也就是c
        // uriResourceParts[1] 是productFeatureId
        if (uriResourceParts.size() == 1 && uriResourceParts.get(0) instanceof UriResourcePrimitiveProperty) { // 这里就是简单单字段
            UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResourceParts.get(0);
            EdmProperty edmProperty = uriResourceProperty.getProperty();
            return edmProperty;
        } else {
            return visitMemberMultiParts(uriResourceParts); // 返回Object，直接返回

        }
    }

    private Object visitMemberMultiParts(List<UriResource> uriResourceParts) throws ODataApplicationException, ExpressionVisitException {
        if (uriResourceParts.get(uriResourceParts.size() - 1) instanceof UriResourceCount) {
            return uriResourceParts.toString();
        }
        int size = uriResourceParts.size();
        // 先判断是不是lambda表达式的any阶段，并且获取any的变量名及any的index
        boolean isLambdaAny = false;
        int lambdaAnyIndex = -1;
        if (uriResourceParts.get(size - 1) instanceof UriResourceLambdaAny) {
            isLambdaAny = true;
            lambdaAnyIndex = size - 1;
        }
//        UriResource firstUriResourcePart = uriResourceParts.get(0);
        // 普通的多段式查询，例如/Contents?$filter=DataResource/dataResourceTypeId eq 'ELECTRONIC_TEXT'
        String lastAlias = joinAlias;
        EdmEntityType lastEdmEntityType = edmEntityType;
        for (int i = 0; i < size; i++) {
            String alias = null;
            UriResource uriResource = uriResourceParts.get(i);
            if (uriResource instanceof UriResourceLambdaAny) {
                UriResourceLambdaAny any = (UriResourceLambdaAny) uriResource;
                // 例如：Products?$filter=ProductFeatureAppl/any(c:c/productFeatureId eq 'SIZE_2' or c/productFeatureId eq 'SIZE_6')
                Object lambdaResult = visitLambdaExpression("ANY", any.getLambdaVariable(), any.getExpression());
                if (!needGroupBy) {
                    needGroupBy = true;
                    groupBySql = " group by " + this.mainTableName + ".id";
                }
                return lambdaResult;
            }
            if (uriResource instanceof UriResourceLambdaVariable) {
                // 例如：Products?$filter=ProductFeatureAppl/any(c:c/productFeatureId eq 'SIZE_2' or c/productFeatureId eq 'SIZE_6')
                UriResourceLambdaVariable lambdaVariable = (UriResourceLambdaVariable) uriResource;
                String variableName = lambdaVariable.getSegmentValue();
                UriResource resourceProperty = uriResourceParts.get(1);
                String propertyName = resourceProperty.getSegmentValue();
                String filterProperty = variableName + "." + Util.javaNameToDbName(propertyName);
                return filterProperty;
            }
            if (isLambdaAny && i == lambdaAnyIndex - 1) { // 代表已经到了any的前一段，也就是any代表的navigation
                // 例如：Products?$filter=ProductFeatureAppl/any(c:c/productFeatureId eq 'SIZE_2' or c/productFeatureId eq 'SIZE_6')
                UriResourceLambdaAny any = (UriResourceLambdaAny) uriResourceParts.get(size - 1);
                alias = any.getLambdaVariable();
                lastEdmEntityType = addJoinTable(lastAlias, lastEdmEntityType, uriResource.getSegmentValue() , alias);
                lastAlias = alias;
            } else if (!(uriResource instanceof UriResourceProperty)) {
                alias = Util.javaNameToDbName(uriResource.getSegmentValue());
                lastEdmEntityType = addJoinTable(lastAlias, lastEdmEntityType, uriResource.getSegmentValue() , alias);
                lastAlias = alias;
            }
        }
        // 最后一段是PropertyName
        UriResource resourceProperty = uriResourceParts.get(size - 1);
        String propertyName = resourceProperty.getSegmentValue();
        String filterProperty = lastAlias + "." + Util.javaNameToDbName(propertyName);
        return filterProperty;
//        throw new ODataApplicationException("Only primitive properties are implemented in filter expressions.",
//                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    private EdmEntityType addJoinTable(String lastAlias, EdmEntityType lastEdmEntityType, String navigationName, String alias) {
        Set<String> aliasSet = tableAlias.keySet();
        if (!aliasSet.contains(alias)) {
//            joinSql = joinSql + Util.addJoinTable(joinSql, lastAlias, lastEdmEntityType, navigationName, alias);
            return Util.addJoinTable(sqlHolder, lastAlias, lastEdmEntityType, navigationName, alias);
        }
        return lastEdmEntityType.getNavigationProperty(navigationName).getType();
    }

    @Override
    public Object visitAlias(String s) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitTypeLiteral(EdmType edmType) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitLambdaReference(String s) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitBinaryOperator(BinaryOperatorKind binaryOperatorKind, Object o, List list) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitEnum(EdmEnumType edmEnumType, List list) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    public String getJoinSql() {
        return sqlHolder.getJoinSql();
    }

    public String getGroupBySql() {
        return groupBySql;
    }
}
