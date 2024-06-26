package com.banfftech.reactodata.edmconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class EdmServiceConfig {
    @JsonProperty("service-name")
    private String serviceName;

    @JsonProperty("name-space")
    private String nameSpace;

    @JsonProperty("entity-types")
    private List<EdmEntityType> entityTypes;
    @JsonProperty("container")
    private Map<String, Object> container;

    public Map<String, Object> getContainer() {
        return container;
    }

    public void setContainer(Map<String, Object> container) {
        this.container = container;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public List<EdmEntityType> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<EdmEntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }
}
