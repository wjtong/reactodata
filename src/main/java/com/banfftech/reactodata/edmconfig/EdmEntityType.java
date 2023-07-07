package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EdmEntityType {
    @JsonProperty("entity-name")
    private String entityName;

    @JsonProperty("quark-entity")
    private String quarkEntity;

    @JsonProperty("property")
    private List<EdmProperty> property;
    @JsonProperty("navigation")
    private List<EdmNavigation> navigation;
    @JsonProperty("action")
    private List<EdmAction> action;

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

    public List<EdmProperty> getProperty() {
        return property;
    }

    public void setProperty(List<EdmProperty> property) {
        this.property = property;
    }

    public List<EdmNavigation> getNavigation() {
        return navigation;
    }

    public void setNavigation(List<EdmNavigation> navigation) {
        this.navigation = navigation;
    }

    public List<EdmAction> getAction() {
        return action;
    }

    public void setAction(List<EdmAction> action) {
        this.action = action;
    }
}
