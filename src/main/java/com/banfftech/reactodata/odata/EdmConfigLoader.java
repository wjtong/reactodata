package com.banfftech.reactodata.odata;

import com.banfftech.reactodata.csdl.QuarkCsdlEntitySet;
import com.banfftech.reactodata.csdl.QuarkCsdlEntityType;
import com.banfftech.reactodata.csdl.QuarkCsdlNavigationProperty;
import com.banfftech.reactodata.csdl.QuarkCsdlProperty;
import com.banfftech.reactodata.edmconfig.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EdmConfigLoader {
    public EdmService loadService(String serviceName) throws IOException, ClassNotFoundException {
        EdmService edmService = new EdmService();
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = Paths.get("config", serviceName + ".json").toString();
        EdmServiceConfig edmServiceConfig = objectMapper.readValue(new File(filePath), EdmServiceConfig.class);

        for (EdmEntityType edmEntityType:edmServiceConfig.getEntityTypes()) {
            QuarkCsdlEntityType quarkCsdlEntityType = loadEntityType(edmServiceConfig, edmEntityType);
            QuarkCsdlEntitySet quarkCsdlEntitySet = loadEntitySet(edmServiceConfig, quarkCsdlEntityType);
            edmService.addEntityType(quarkCsdlEntityType);
            edmService.addEntitySet(quarkCsdlEntitySet);
        }
        edmService.setNamespace(edmServiceConfig.getNameSpace());
        return edmService;
    }

    private QuarkCsdlEntitySet loadEntitySet(EdmServiceConfig edmServiceConfig, QuarkCsdlEntityType quarkCsdlEntityType) {
        QuarkCsdlEntitySet quarkCsdlEntitySet = new QuarkCsdlEntitySet();
        quarkCsdlEntitySet.setName(quarkCsdlEntityType.getName());
        quarkCsdlEntitySet.setType(new FullQualifiedName(edmServiceConfig.getNameSpace(), quarkCsdlEntityType.getName()));
        return quarkCsdlEntitySet;
    }

    private QuarkCsdlEntityType loadEntityType(EdmServiceConfig edmServiceConfig, EdmEntityType edmEntityType) throws ClassNotFoundException {
        String entityName = edmEntityType.getEntityName();
        String quarkEntity = entityName;
        String draftEntityName = null;
        String attrEntityName = null;
        String attrNumericEntityName = null;
        String attrDateEntityName = null;
        String handlerClass = null;
        String entityConditionStr = null;
        String searchOption = null;
        if (edmEntityType.getQuarkEntity() != null) {
            quarkEntity = edmEntityType.getQuarkEntity();
        }
        List<CsdlProperty> csdlProperties = loadProperties(edmEntityType);
        List<CsdlNavigationProperty> csdlNavigationProperties = loadNavigation(edmEntityType);
        List<CsdlPropertyRef> csdlPropertyRefs = null;
        boolean filterByDate = false;
        String labelPrefix = entityName;
        boolean hasDerivedEntity = false;
        //是否自动生成所有Property的Label
        List<String> excludeProperties = new ArrayList<>();
        FullQualifiedName fullQualifiedName = new FullQualifiedName(edmServiceConfig.getNameSpace(), entityName);
        boolean hasRelField = false;
        QuarkCsdlEntityType csdlEntityType = createEntityType(fullQualifiedName, quarkEntity,
                draftEntityName, attrEntityName, attrNumericEntityName, attrDateEntityName, handlerClass, true,
                csdlProperties, csdlNavigationProperties, csdlPropertyRefs, filterByDate, hasDerivedEntity,
                excludeProperties, entityConditionStr, labelPrefix, searchOption);
        return csdlEntityType;
    }

    private List<CsdlNavigationProperty> loadNavigation(EdmEntityType edmEntityType) {
        List<EdmNavigation> edmNavigations = edmEntityType.getNavigations();
        if (edmNavigations == null) {
            return null;
        }
        List<CsdlNavigationProperty> csdlNavigationProperties = new ArrayList<>();
        for (EdmNavigation edmNavigation:edmNavigations) {
            QuarkCsdlNavigationProperty quarkCsdlNavigationProperty = new QuarkCsdlNavigationProperty();
            quarkCsdlNavigationProperty.setName(edmNavigation.getPropertyName());
            quarkCsdlNavigationProperty.setType(new FullQualifiedName(EdmConst.NAMESPACE, edmNavigation.getNavigationType()));
            if (edmNavigation.getProperty() != null) {
                CsdlReferentialConstraint csdlReferentialConstraint = new CsdlReferentialConstraint();
                csdlReferentialConstraint.setProperty(edmNavigation.getProperty());
                if (edmNavigation.getRefProperty() != null) {
                    csdlReferentialConstraint.setReferencedProperty(edmNavigation.getRefProperty());
                } else {
                    csdlReferentialConstraint.setReferencedProperty(edmNavigation.getProperty());
                }
                quarkCsdlNavigationProperty.setReferentialConstraints(List.of(csdlReferentialConstraint));
            }
            String relation = edmNavigation.getRelation();
            if ("one".equals(relation)) {
                quarkCsdlNavigationProperty.setCollection(false);
            } else if ("many".equals(relation)) {
                quarkCsdlNavigationProperty.setCollection(true);
            }
            csdlNavigationProperties.add(quarkCsdlNavigationProperty);
        }
        return csdlNavigationProperties;
    }

    private List<CsdlProperty> loadProperties(EdmEntityType edmEntityType) {
        List<EdmProperty> edmProperties = edmEntityType.getProperties();
        List<CsdlProperty> csdlProperties = new ArrayList<>();
        for (EdmProperty edmProperty:edmProperties) {
            QuarkCsdlProperty quarkCsdlProperty = new QuarkCsdlProperty();
            quarkCsdlProperty.setName(edmProperty.getPropertyName());
            quarkCsdlProperty.setType(DataMapper.FIELDMAP.get(edmProperty.getPropertyType()).getFullQualifiedName());
            csdlProperties.add(quarkCsdlProperty);
        }
        return csdlProperties;
    }

    private static QuarkCsdlEntityType createEntityType(FullQualifiedName entityTypeFqn, String quarkEntity,
                                                        String draftEntityName, String attrEntityName, String attrNumericEntityName, String attrDateEntityName,
                                                        String handlerClass, boolean autoProperties,
                                                        List<CsdlProperty> csdlProperties,
                                                        List<CsdlNavigationProperty> csdlNavigationProperties,
                                                        List<CsdlPropertyRef> csdlPropertyRefs, boolean filterByDate,
                                                        boolean hadDerivedEntity, List<String> excludeProperties,
                                                        String entityConditionStr,
                                                        String labelPrefix, String searchOption) throws ClassNotFoundException {
        String entityName = entityTypeFqn.getName(); // Such as Invoice
        List<CsdlPropertyRef> propertyRefs = csdlPropertyRefs = new ArrayList<>();
        if (autoProperties) {
            // 添加主键
            QuarkCsdlProperty csdlProperty = new QuarkCsdlProperty();
            csdlProperty.setName("id");
            csdlProperty.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            csdlProperties.add(csdlProperty);
        }
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("id");
        propertyRefs.add(propertyRef);
        QuarkCsdlEntityType entityType = new QuarkCsdlEntityType();
        entityType.setTableName(quarkEntity);
        entityType.setName(entityName);
        entityType.setProperties(csdlProperties);
        if (csdlNavigationProperties != null) {
            entityType.setNavigationProperties(csdlNavigationProperties);
        }
        if (propertyRefs != null) {
            entityType.setKey(propertyRefs);
        }
        return entityType;
    }
    private static CsdlAbstractEdmItem generatePropertyFromField(Field field, boolean autoEnum) {
        if (field == null) {
            return null;
        }
        Type fieldType = field.getGenericType();
        String fieldTypeName = fieldType.getTypeName();
        String fieldName = field.getName();

        QuarkCsdlProperty csdlProperty = new QuarkCsdlProperty();
        csdlProperty.setName(fieldName);
        if (fieldTypeName.equals("java.lang.String")) {
            csdlProperty.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        } else if (fieldTypeName.equals("java.time.LocalDate")) {
            csdlProperty.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
        } else if (fieldTypeName.equals("boolean")) {
            csdlProperty.setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
        } else if (fieldTypeName.startsWith("java.util.List")) {
            String fieldStr = field.toGenericString();
            String packageClassName = fieldStr.substring(fieldStr.indexOf("<") + 1, fieldStr.indexOf(">"));
            String className = packageClassName.substring(packageClassName.lastIndexOf(".") + 1);
            String fqn = EdmConst.NAMESPACE + "." + className;
            QuarkCsdlNavigationProperty csdlNavigationProperty = new QuarkCsdlNavigationProperty();
            csdlNavigationProperty.setType(new FullQualifiedName(fqn));
            csdlNavigationProperty.setCollection(true);
            csdlNavigationProperty.setName(fieldName);
            return csdlNavigationProperty;
        } else {
            return null;
        }
        return csdlProperty;
    }
}
