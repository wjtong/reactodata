package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.Util;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.apache.olingo.server.core.uri.UriResourcePrimitivePropertyImpl;

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
    private EdmEntityType joinEdmEntityType;
    private String joinAlias;
    private Map<String, String> tableAlias = new HashMap<>();
    private String fromSql;
    private String groupBySql;

    public OdataExpressionVisitor(EdmEntityType edmEntityType) {
        this.edmEntityType = edmEntityType;
        String tableName = Util.javaNameToDbName(edmEntityType.getName());
        this.fromSql = tableName;
        this.joinEdmEntityType = edmEntityType;
        this.joinAlias = tableName;
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
//        UriResource firstUriResourcePart = uriResourceParts.get(0);
        // 普通的多段式查询，例如/Contents?$filter=DataResource/dataResourceTypeId eq 'ELECTRONIC_TEXT'
        List<String> resourceParts = new ArrayList<>();
        String lastAlias = joinAlias;
        EdmEntityType lastEdmEntityType = edmEntityType;
        for (int i = 0; i < size; i++) {
            UriResource uriResource = uriResourceParts.get(i);
            if (uriResource instanceof UriResourceLambdaAny) {
                UriResourceLambdaAny any = (UriResourceLambdaAny) uriResource;
                // 例如：Products?$filter=ProductFeatureAppl/any(c:c/productFeatureId eq 'SIZE_2' or c/productFeatureId eq 'SIZE_6')
                Object lambdaResult = visitLambdaExpression("ANY", any.getLambdaVariable(), any.getExpression());
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
            resourceParts.add(uriResource.getSegmentValue());
            lastEdmEntityType = addJoinTable(lastAlias, lastEdmEntityType, uriResource.getSegmentValue());
            lastAlias = Util.javaNameToDbName(lastEdmEntityType.getName());
        }
//        lastAlias = addMultiParts(resourceParts);
        // 最后一段是PropertyName
        UriResource resourceProperty = uriResourceParts.get(size - 1);
        String propertyName = resourceProperty.getSegmentValue();
        String filterProperty = lastAlias + "." + Util.javaNameToDbName(propertyName);
        return filterProperty;


//        throw new ODataApplicationException("Only primitive properties are implemented in filter expressions.",
//                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    private EdmEntityType addJoinTable(String lastAlias, EdmEntityType lastEdmEntityType, String navigationName) {
        Set<String> aliasSet = tableAlias.keySet();
        EdmNavigationProperty edmNavigationProperty = lastEdmEntityType.getNavigationProperty(navigationName);
        if (edmNavigationProperty != null) {
            EdmEntityType targetEntityType = edmNavigationProperty.getType();
            String targetTableName = Util.javaNameToDbName(targetEntityType.getName());
            List<EdmReferentialConstraint> referentialConstraints = edmNavigationProperty.getReferentialConstraints();
            EdmReferentialConstraint referentialConstraint = referentialConstraints.get(0); // only support one constraint
            String sourceColumnName = Util.javaNameToDbName(referentialConstraint.getPropertyName());
            String targetColumnName = Util.javaNameToDbName(referentialConstraint.getReferencedPropertyName());
            if (!aliasSet.contains(targetTableName)) {
                fromSql = fromSql + " left join " + targetTableName + " on " + lastAlias + "." + sourceColumnName + "=" + targetTableName + "." + targetColumnName;
                tableAlias.put(targetTableName, targetTableName);
            }
            return targetEntityType;
        } else {
            throw new RuntimeException("not support");
        }
    }

    private String addMultiParts(List<String> resourceParts) {
        int index = 0;
        String sourceTableName = Util.javaNameToDbName(joinEdmEntityType.getName());
        Set<String> aliasSet = tableAlias.keySet();
        for (String resourcePart : resourceParts) {
            EdmNavigationProperty edmNavigationProperty = joinEdmEntityType.getNavigationProperty(resourcePart);
            if (edmNavigationProperty != null) {
                EdmEntityType targetEntityType = edmNavigationProperty.getType();
                String targetTableName = Util.javaNameToDbName(targetEntityType.getName());
                List<EdmReferentialConstraint> referentialConstraints = edmNavigationProperty.getReferentialConstraints();
                EdmReferentialConstraint referentialConstraint = referentialConstraints.get(0); // only support one constraint
                String sourceColumnName = Util.javaNameToDbName(referentialConstraint.getPropertyName());
                String targetColumnName = Util.javaNameToDbName(referentialConstraint.getReferencedPropertyName());
                if (!aliasSet.contains(targetTableName)) {
                    fromSql = fromSql + " left join " + targetTableName + " on " + sourceTableName + "." + sourceColumnName + "=" + targetTableName + "." + targetColumnName;
                    tableAlias.put(targetTableName, targetTableName);
                }
                if (index == resourceParts.size() - 2) {
                    break;
                }
                sourceTableName = targetTableName;
                index++;
            } else {
                throw new RuntimeException("not support");
            }
        }
        return sourceTableName;
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

    public String getFromSql() {
        return fromSql;
    }
}
