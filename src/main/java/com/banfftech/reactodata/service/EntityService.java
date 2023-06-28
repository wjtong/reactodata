package com.banfftech.reactodata.service;

import com.banfftech.reactodata.odata.QuarkEntity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;

import java.util.List;
import java.util.Map;

public interface EntityService {
    // Based on the input parameter FilterOption which is OData query option, return the find result of the query
    List<QuarkEntity> findEntity(EdmEntityType edmEntityType, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    List<QuarkEntity> findRelatedEntity(QuarkEntity entity, EdmEntityType targetEdmEntityType, Map<String, String> mappedProperties, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    QuarkEntity findEntityById(String entityName, String id, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
}
