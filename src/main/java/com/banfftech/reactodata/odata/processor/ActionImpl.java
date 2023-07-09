package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.Util;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.*;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import java.util.List;
import java.util.Map;

public class ActionImpl implements ActionVoidProcessor, ActionEntityCollectionProcessor, ActionEntityProcessor,
        ActionPrimitiveProcessor, ActionPrimitiveCollectionProcessor, ActionComplexProcessor, ActionComplexCollectionProcessor{
    @Override
    public void processActionComplexCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void processActionComplex(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void processActionEntityCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void processActionEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {
        final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        Map<String, QueryOption> queryOptions = Util.getQueryOptions(uriInfo);
        SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
        ExpandOption expandOption = (ExpandOption) queryOptions.get("expandOption");
        UriResourceAction uriResourceAction = (UriResourceAction) resourcePaths.get(resourcePaths.size() - 1);
        final EdmAction edmAction = uriResourceAction.getAction();
        final EdmEntityType edmReturnEntityType = (EdmEntityType) edmAction.getReturnType().getType();
        EdmEntitySet boundEntitySet = null;
        EdmEntitySet returnedEntitySet = null;

    }

    @Override
    public void processActionPrimitiveCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void processActionPrimitive(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void processActionVoid(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {

    }
}
