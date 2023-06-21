package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.Util;
import com.banfftech.reactodata.odata.QuarkEntity;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntityImpl implements EntityProcessor {
    private OData odata;
    private ServiceMetadata serviceMetadata;
    private QuarkProcessor quarkProcessor;
    public EntityImpl(QuarkProcessor quarkProcessor) {
        this.quarkProcessor = quarkProcessor;
    }
    @Override
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        try {
            List<UriResource> uriResourceParts = uriInfo.getUriResourceParts();
            Map<String, QueryOption> queryOptions = Util.getQuernOptions(uriInfo);
            QuarkEntity entity = null;
            EdmEntityType targetEntityType = null;
            EdmBindingTarget edmBindingTarget = null;
            int resourcePartsSize = uriResourceParts.size();
            int i = 0;
            for (UriResource uriResource:uriResourceParts) {
                Map<String, QueryOption> useQueryOptions = null;
                if (i == resourcePartsSize - 1) { // 只有到最后一段采用queryOptions
                    useQueryOptions = queryOptions;
                }
                if (uriResource instanceof UriResourceEntitySet) {
                    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
                    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
                    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
                    List<UriParameter> keyParameters = uriResourceEntitySet.getKeyPredicates();
                    if (keyParameters != null && keyParameters.size() > 0) {
                        entity = quarkProcessor.findOne(edmEntityType, keyParameters, useQueryOptions);
                    }
                    targetEntityType = edmEntityType;
                    edmBindingTarget = edmEntitySet;
                } else if (uriResource instanceof UriResourceNavigation) { // 前面肯定有entity查出来了
                    UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) uriResource;
                    EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                    EdmEntityType edmEntityType = edmNavigationProperty.getType();
                    List<UriParameter> keyPredicates = uriResourceNavigation.getKeyPredicates();
                    if (keyPredicates != null && keyPredicates.size() > 0) {
                        entity = quarkProcessor.findOne(targetEntityType, keyPredicates, useQueryOptions);
                    } else {
                        entity = quarkProcessor.findRelatedOne(entity, edmNavigationProperty, useQueryOptions);
                    }
                    targetEntityType = edmEntityType;
                    edmBindingTarget = edmBindingTarget.getRelatedBindingTarget(edmNavigationProperty.getName());
                }
            }
            serializeEntity(oDataRequest, oDataResponse, edmBindingTarget, targetEntityType,
                    contentType, entity, queryOptions);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ODataApplicationException("Error processing the request", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH, e);
        }

    }

    @Override
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.odata = oData;
        this.serviceMetadata = serviceMetadata;
    }
    private void serializeEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, EdmBindingTarget edmBindingTarget,
                                 EdmEntityType edmEntityType, ContentType contentType,
                                 Entity entity, Map<String, QueryOption> queryOptions)
            throws SerializerException {
        ExpandOption expandOption = (ExpandOption) queryOptions.get("expandOption");
        SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
        //Remove stream property
        entity.getProperties().removeIf(property -> "Edm.Stream".equals(property.getType()));
        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
        String typeName = edmBindingTarget != null ? edmBindingTarget.getName() : edmEntityType.getName();
        try {
            ContextURL contextUrl = ContextURL.with().serviceRoot(new URI(oDataRequest.getRawBaseUri() + "/"))
                    .entitySetOrSingletonOrType(typeName).selectList(selectList).suffix(ContextURL.Suffix.ENTITY).build();
            // expand and select currently not supported
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption)
                    .expand(expandOption).build();
            ODataSerializer serializer = odata.createSerializer(contentType);
            SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
            // configure the response object
            oDataResponse.setContent(serializerResult.getContent());
            oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
            oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
