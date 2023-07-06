package com.banfftech.reactodata.odata;

import com.banfftech.reactodata.csdl.QuarkCsdlSchema;
import com.banfftech.reactodata.edmconfig.EdmConst;
import io.quarkus.cache.CacheResult;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EdmProvider implements CsdlEdmProvider {

    private static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(EdmConst.NAMESPACE, EdmConst.CONTAINER_NAME);
    private static QuarkCsdlSchema csdlSchema = null;
    private String serviceName;
//    @Inject
    EdmConfigLoader edmConfigLoader;

    public EdmProvider(EdmConfigLoader edmConfigLoader, String serviceName) {
        this.edmConfigLoader = edmConfigLoader;
        this.serviceName = serviceName;
    }

    public void loadService() {
        try {
            EdmService edmService = edmConfigLoader.loadService(serviceName);
            csdlSchema = this.createSchema(edmService);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ODataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        if (csdlSchema != null) {
            CsdlEntityType csdlEntityType = csdlSchema.getEntityType(entityTypeName.getName());
            return csdlEntityType;
        }
        return null;
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public CsdlTerm getTerm(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        if (csdlSchema != null) {
            return csdlSchema.getEntityContainer().getEntitySet(entitySetName);
        }
        return null;
    }

    @Override
    public CsdlSingleton getSingleton(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        if (csdlSchema != null) {
            return csdlSchema.getEntityContainer();
        }
        return null;
    }

    @Override
    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        if (entityContainerName == null || entityContainerName.equals(CONTAINER_FQN)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER_FQN);

            return entityContainerInfo;
        }

        return null;
    }

    @Override
    public List<CsdlAliasInfo> getAliasInfos() throws ODataException {
        return null;
    }

    @Override
    @CacheResult(cacheName = "csdlSchemas")
    public List<CsdlSchema> getSchemas() {
        if (this.csdlSchema == null) {
            loadService();
        }
        // Return schema in a list
        return Collections.singletonList(this.csdlSchema);
    }

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) {
        // We don't have any EnumTypes in this example
        return null;
    }

    @Override
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName typeDefinitionName) {
        // We don't have any TypeDefinitions in this example
        return null;
    }

    private QuarkCsdlSchema createSchema(EdmService edmService) throws ODataException {
        // create Schema
        QuarkCsdlSchema schema = new QuarkCsdlSchema();
        schema.setNamespace(edmService.getNamespace());

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.addAll(edmService.getEntityTypes());
        // schema.setEntityTypes(edmConfig.getEntityTypes(edmWebConfig));
        // add complex types
        List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
        complexTypes.addAll(edmService.getComplexTypes());
        // add enum types
        List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
        enumTypes.addAll(edmService.getEnumTypes());
        // add functions
        List<CsdlFunction> functions = new ArrayList<CsdlFunction>();
        functions.addAll(edmService.getFunctions());
        // add actions
        List<CsdlAction> actions = new ArrayList<CsdlAction>();
        actions.addAll(edmService.getActions());
        // add EntityContainer
//		schema.setEntityContainer(getEntityContainer());
        schema.setEntityContainer(this.createEntityContainer(edmService));
        // add Annotations
        List<CsdlAnnotations> annotationses = new ArrayList<CsdlAnnotations>();
        annotationses.addAll(edmService.getAnnotationses());
        // add Terms
        List<CsdlTerm> terms = new ArrayList<CsdlTerm>();
        terms.addAll(edmService.getTerms());

        schema.setEntityTypes(entityTypes);
        schema.setComplexTypes(complexTypes);
        schema.setEnumTypes(enumTypes);
        schema.setFunctions(functions);
        schema.setActions(actions);
        schema.setAnnotationsGroup(annotationses);
        schema.setTerms(terms);

        return schema;
    }

    private CsdlEntityContainer createEntityContainer(EdmService edmService) throws ODataException {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(EdmConst.CONTAINER_NAME);

        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.addAll(edmService.getEntitySets());

        List<CsdlFunctionImport> functionImports = new ArrayList<CsdlFunctionImport>();
        functionImports.addAll(edmService.getFunctionImports());

        List<CsdlActionImport> actionImports = new ArrayList<CsdlActionImport>();
        actionImports.addAll(edmService.getActionImports());

        List<CsdlSingleton> singletons = new ArrayList<CsdlSingleton>();
        singletons.addAll(edmService.getSingletons());

        entityContainer.setEntitySets(entitySets);
        entityContainer.setFunctionImports(functionImports);
        entityContainer.setActionImports(actionImports);
        entityContainer.setSingletons(singletons);

        return entityContainer;
    }
}