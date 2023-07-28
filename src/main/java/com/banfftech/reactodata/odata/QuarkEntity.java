package com.banfftech.reactodata.odata;

import com.banfftech.reactodata.Util;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;

import java.math.BigDecimal;

public class QuarkEntity extends Entity {
    public static QuarkEntity from(Row row) {
        QuarkEntity quarkEntity = new QuarkEntity();
        int size = row.size();
        for (int i = 0; i < size; i++) {
            String columnName = row.getColumnName(i);
            String varName = Util.dbNameToVarName(columnName);
            Object value = row.getValue(i);
            Property property = null;
            if (value instanceof String) {
                property = new Property("Edm.String", varName, ValueType.PRIMITIVE, value);
            } else if (value instanceof BigDecimal) {
                property = new Property("Edm.Decimal", varName, ValueType.PRIMITIVE, value);
            } else if (value instanceof Numeric) {
                Numeric numeric = (Numeric) value;
                property = new Property("Edm.Decimal", varName, ValueType.PRIMITIVE, numeric.bigDecimalValue());
            } else if (value instanceof Integer) {
                property = new Property("Edm.Int32", varName, ValueType.PRIMITIVE, value);
            } else if (value instanceof Long) {
                property = new Property("Edm.Int64", varName, ValueType.PRIMITIVE, value);
            } else if (value instanceof Boolean) {
                property = new Property("Edm.Boolean", varName, ValueType.PRIMITIVE, value);
            } else if (value instanceof Double) {
                property = new Property("Edm.Double", varName, ValueType.PRIMITIVE, value);
            } else if (value instanceof Float) {
                property = new Property("Edm.Single", varName, ValueType.PRIMITIVE, value);
            } else {
                // TODO: other types
            }
            quarkEntity.addProperty(property);
        }
        return quarkEntity;
    }
}
