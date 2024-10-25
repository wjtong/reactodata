package com.banfftech.reactodata.odata;

import com.banfftech.reactodata.Util;
import com.banfftech.reactodata.odata.processor.ActionImpl;
import com.banfftech.reactodata.odata.processor.EntityCollectionImp;
import com.banfftech.reactodata.odata.processor.EntityImpl;
import com.banfftech.reactodata.odata.processor.QuarkProcessor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

@Path("/odata.svc")
public class ODataResource {
    private static final Logger LOGGER = Logger.getLogger(ODataResource.class.getName());
    @Inject
    EdmConfigLoader edmConfigLoader;
    @Inject
    QuarkProcessor quarkProcessor;
    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{serviceName}/$metadata")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMetadata(@PathParam("serviceName") String serviceName) {
        try {
            EdmProvider edmProvider = new EdmProvider(edmConfigLoader, serviceName);
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            ODataSerializer serializer = odata.createSerializer(ContentType.APPLICATION_XML);
            SerializerResult serializerResult = serializer.metadataDocument(serviceMetadata);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(serializerResult.getContent(), outputStream);

            return Response.ok(outputStream.toByteArray()).type(ContentType.APPLICATION_XML.toContentTypeString()).build();
        } catch (ODataException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while generating the OData metadata document: " + e.getMessage())
                    .build();
        }
    }
    @GET
    @Path("reactive/$metadata")
    @Produces(MediaType.APPLICATION_XML)
    public Response getTestMetadata() {
        try {
            String serviceName = extractServiceName();
            EdmProvider edmProvider = new EdmProvider(edmConfigLoader, serviceName);
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            ODataSerializer serializer = odata.createSerializer(ContentType.APPLICATION_XML);
            SerializerResult serializerResult = serializer.metadataDocument(serviceMetadata);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(serializerResult.getContent(), outputStream);

            return Response.ok(outputStream.toByteArray()).type(ContentType.APPLICATION_XML.toContentTypeString()).build();
        } catch (ODataException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while generating the OData metadata document: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{serviceName}/")
    public Response serviceRoot() {
        return Response.ok().build();
    }

    @GET
    @Path("/{serviceName}/{odataPath:.+}")
    public Response processODataRequest(@PathParam("odataPath") String odataPath,
                                        @PathParam("serviceName") String serviceName,
                                        @QueryParam("$filter") String filter,
                                        @QueryParam("$expand") String expand,
                                        @QueryParam("$select") String select,
                                        @QueryParam("$orderby") String orderby,
                                        @QueryParam("$top") String top,
                                        @QueryParam("$skip") String skip,
                                        @QueryParam("$count") String count,
                                        @QueryParam("$search") String search,
                                        @QueryParam("$apply") String apply) {
        ODataRequest request = new ODataRequest();
        String baseUri = uriInfo.getBaseUri().toString();
//        String baseUri = "http://localhost:8080/odata.svc";
        String queryString = Util.getQueryString(filter, expand, select, orderby, top, skip);
        request.setRawBaseUri(baseUri);
        request.setRawODataPath(odataPath);
        request.setRawServiceResolutionUri("/");
        request.setMethod(org.apache.olingo.commons.api.http.HttpMethod.valueOf(HttpMethod.GET));
        request.setRawRequestUri(baseUri + "/" + serviceName + "/" + odataPath + (queryString != null ? "?" + queryString : ""));
        request.setRawQueryPath(queryString);

        ODataResponse response = new ODataResponse();

        try {
            EdmProvider edmProvider = new EdmProvider(edmConfigLoader, serviceName);
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            ODataHandler handler = odata.createRawHandler(serviceMetadata);
            handler.register(new EntityCollectionImp(quarkProcessor));
            handler.register(new EntityImpl(quarkProcessor));
            response = handler.process(request);
        } catch (NotSupportedException e) {
            LOGGER.severe("Method not allowed: " + e.getMessage());
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        } catch (Exception e) {
            LOGGER.severe("Internal server error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // Return the response
        return Response.status(response.getStatusCode())
                .entity(response.getContent())
                .type(response.getHeader(HttpHeader.CONTENT_TYPE).toString())
                .build();
    }
    @POST
    @Path("/{odataPath:.+}")
    public Response processODataPost(@PathParam("odataPath") String odataPath,
                                        @QueryParam("$expand") String expand,
                                        @QueryParam("$select") String select,
                                        @QueryParam("$orderby") String orderby,
                                        @QueryParam("$top") String top,
                                        @QueryParam("$skip") String skip) {
        ODataRequest request = new ODataRequest();
        String baseUri = uriInfo.getBaseUri().toString();
//        String baseUri = "http://localhost:8080/odata.svc";
        String queryString = Util.getQueryString(null, expand, select, orderby, top, skip);
        request.setRawBaseUri(baseUri);
        request.setRawODataPath(odataPath);
        request.setRawServiceResolutionUri("/");
        request.setMethod(org.apache.olingo.commons.api.http.HttpMethod.valueOf(HttpMethod.POST));
        request.setRawRequestUri(baseUri + odataPath + (queryString != null ? "?" + queryString : ""));
        request.setRawQueryPath(queryString);
        request.setBody(null);

        ODataResponse response = new ODataResponse();

        try {
            String serviceName = extractServiceName();
            EdmProvider edmProvider = new EdmProvider(edmConfigLoader, serviceName);
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            ODataHandler handler = odata.createRawHandler(serviceMetadata);
            handler.register(new ActionImpl(quarkProcessor));
            handler.register(new EntityImpl(quarkProcessor));
            response = handler.process(request);
        } catch (NotSupportedException e) {
            LOGGER.severe("Method not allowed: " + e.getMessage());
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        } catch (Exception e) {
            LOGGER.severe("Internal server error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // Return the response
        return Response.status(response.getStatusCode())
                .entity(response.getContent())
                .type(response.getHeader(HttpHeader.CONTENT_TYPE))
                .build();
    }

    private String extractServiceName() {
        String path = uriInfo.getPath();
        String[] segments = path.split("/");
        if (segments.length > 1) {
            return segments[1]; // Assuming the service name is the second segment
        }
        return "defaultServiceName"; // Fallback if not found
    }
}
