package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdmProperty {
    @JsonProperty("name")
    private String propertyName;
    @JsonProperty("type")
    private String propertyType;

    @JsonProperty("required")
    private boolean required = false;

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
