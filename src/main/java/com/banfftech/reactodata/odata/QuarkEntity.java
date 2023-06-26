package com.banfftech.reactodata.odata;

import com.banfftech.reactodata.Util;
import io.vertx.mutiny.sqlclient.Row;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;

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
            } else {
                // TODO: other types
            }
            quarkEntity.addProperty(property);
        }
        return quarkEntity;
    }
}
