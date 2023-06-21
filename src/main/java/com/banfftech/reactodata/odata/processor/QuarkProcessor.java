package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.odata.QuarkEntity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;

import java.util.List;
import java.util.Map;

public interface QuarkProcessor {
    public EntityCollection findList(EdmEntityType edmEntityType, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    public QuarkEntity findOne(EdmEntityType edmEntityType, List<UriParameter> keyPredicates, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    public QuarkEntity findRelatedOne(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    public EntityCollection findRelatedList(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
}
