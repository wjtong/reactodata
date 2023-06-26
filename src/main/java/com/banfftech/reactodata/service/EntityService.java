package com.banfftech.reactodata.service;

import com.banfftech.reactodata.odata.QuarkEntity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;

import java.util.List;
import java.util.Map;

public interface EntityService {
    // Based on the input parameter FilterOption which is OData query option, return the find result of the query
    List<QuarkEntity> findEntity(String entityName, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    List<QuarkEntity> findRelatedEntity(QuarkEntity entity, String targetEntityName, Map<String, String> mappedProperties, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    QuarkEntity findEntityById(String entityName, String id) throws ODataApplicationException;
}
