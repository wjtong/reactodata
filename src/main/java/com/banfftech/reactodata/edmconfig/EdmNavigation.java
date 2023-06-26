package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdmNavigation {
    @JsonProperty("name")
    private String propertyName;
    @JsonProperty("type")
    private String navigationType;
    @JsonProperty("relation")
    private String relation;
    @JsonProperty("property")
    private String property;
    @JsonProperty("ref-property")
    private String refProperty;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getNavigationType() {
        return navigationType;
    }

    public void setNavigationType(String propertyType) {
        this.navigationType = propertyType;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getRefProperty() {
        return refProperty;
    }

    public void setRefProperty(String refProperty) {
        this.refProperty = refProperty;
    }
}
