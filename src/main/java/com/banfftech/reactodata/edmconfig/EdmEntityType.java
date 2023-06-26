package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EdmEntityType {
    @JsonProperty("entity-name")
    private String entityName;

    @JsonProperty("quark-entity")
    private String quarkEntity;

    @JsonProperty("properties")
    private List<EdmProperty> properties;
    @JsonProperty("navigation")
    private List<EdmNavigation> navigations;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getQuarkEntity() {
        return quarkEntity;
    }

    public void setQuarkEntity(String quarkEntity) {
        this.quarkEntity = quarkEntity;
    }

    public List<EdmProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<EdmProperty> properties) {
        this.properties = properties;
    }

    public List<EdmNavigation> getNavigations() {
        return navigations;
    }

    public void setNavigations(List<EdmNavigation> navigations) {
        this.navigations = navigations;
    }
}
