package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdmEntityType {
    @JsonProperty("entity-name")
    private String entityName;

    @JsonProperty("quark-entity")
    private String quarkEntity;

    @JsonProperty("auto-properties")
    private boolean autoProperties;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean isAutoProperties() {
        return autoProperties;
    }

    public void setAutoProperties(boolean autoProperties) {
        this.autoProperties = autoProperties;
    }

    public String getQuarkEntity() {
        return quarkEntity;
    }

    public void setQuarkEntity(String quarkEntity) {
        this.quarkEntity = quarkEntity;
    }
}
