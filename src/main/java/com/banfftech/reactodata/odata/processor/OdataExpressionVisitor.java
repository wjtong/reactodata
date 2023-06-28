package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.Util;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        return null;
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
            throw new ODataApplicationException("Only primitive properties are implemented in filter expressions.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
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
}
