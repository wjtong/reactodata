package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EdmAction {
    @JsonProperty("name")
    private String actionName;
    @JsonProperty("return")
    private String returnType;
    @JsonProperty("required")
    private boolean required = false;
    @JsonProperty("parameter")
    private List<EdmParameter> parameter;
    @JsonProperty("collection")
    private boolean collection = false;

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<EdmParameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<EdmParameter> parameter) {
        this.parameter = parameter;
    }
}
