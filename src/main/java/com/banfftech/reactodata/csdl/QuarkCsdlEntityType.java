package com.banfftech.reactodata.csdl;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

public class QuarkCsdlEntityType extends CsdlEntityType {
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
