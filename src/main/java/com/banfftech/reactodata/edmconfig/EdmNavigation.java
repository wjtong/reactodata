package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdmNavigation {
    @JsonProperty("name")
    private String propertyName;
    @JsonProperty("type")
    private String navigationType;
    @JsonProperty("relation")
    private String relation;
    @JsonProperty("mapped-by")
    private String mappedBy;

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

    public String getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }
}
