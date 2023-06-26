package com.banfftech.reactodata.odata.processor;

import com.banfftech.reactodata.Util;
import com.banfftech.reactodata.edmconfig.EdmConst;
import com.banfftech.reactodata.odata.QuarkEntity;
import com.banfftech.reactodata.service.EntityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.core.uri.queryoption.ExpandOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.LevelsOptionImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class QuarkProcessorImpl implements QuarkProcessor{
    @Inject
    EntityService entityService;

    @Override
    public EntityCollection findList(EdmEntityType edmEntityType, Map<String,
            QueryOption> queryOptions) throws ODataApplicationException {
        String entityName = edmEntityType.getName();
        List<QuarkEntity> quarkEntities = entityService.findEntity(entityName, queryOptions);
        EntityCollection entityCollection = new EntityCollection();
        entityCollection.setCount(quarkEntities.size());
        entityCollection.getEntities().addAll(quarkEntities);
        SkipOption skipOption = queryOptions != null? (SkipOption) queryOptions.get("skipOption"):null;
        TopOption topOption = queryOptions != null? (TopOption) queryOptions.get("topOption"):null;
        Util.pageEntityCollection(entityCollection, skipOption != null? skipOption.getValue():0, topOption != null? topOption.getValue(): EdmConst.MAX_ROWS);
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), entityCollection.getEntities(), edmEntityType);
        }
        return entityCollection;
    }

    @Override
    public QuarkEntity findOne(EdmEntityType edmEntityType, List<UriParameter> keyPredicates,
                          Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        String entityName = edmEntityType.getName();
        String regexp = "\'";
        String keyText = keyPredicates.get(0).getText();
        String id = keyText.replaceAll(regexp, "");
        QuarkEntity quarkEntity = entityService.findEntityById(entityName, id);
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), List.of(quarkEntity), edmEntityType);
        }
        return quarkEntity;
    }

    @Override
    public QuarkEntity findRelatedOne(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty,
                                 Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        Map<String, String> mappedProperties = retrieveMappedProperties(edmNavigationProperty);
        String targetEntityName = edmNavigationProperty.getType().getName();
        List<QuarkEntity> quarkEntities = entityService.findRelatedEntity(entity, targetEntityName, mappedProperties, queryOptions);
        if (quarkEntities != null && quarkEntities.size() > 0) {
            EdmEntityType edmEntityType = (EdmEntityType) edmNavigationProperty.getType();
            QuarkEntity relatedEntity = quarkEntities.get(0);
            if (queryOptions != null && queryOptions.get("expandOption") != null) {
                addExpandOption((ExpandOption) queryOptions.get("expandOption"), List.of(relatedEntity), edmEntityType);
            }
            return relatedEntity;
        }
        return null;
    }
    private Map<String, String> retrieveMappedProperties(EdmNavigationProperty edmNavigationProperty) {
        List<EdmReferentialConstraint> edmReferentialConstraints = edmNavigationProperty.getReferentialConstraints();
        Map<String, String> mappedProperties = new HashMap<>();
        for (EdmReferentialConstraint edmReferentialConstraint:edmReferentialConstraints) {
            mappedProperties.put(edmReferentialConstraint.getPropertyName(), edmReferentialConstraint.getReferencedPropertyName());
        }
        return mappedProperties;
    }

    private void addExpandOption(ExpandOption expandOption, List<Entity> entities,
                                 EdmEntityType edmEntityType) throws ODataApplicationException {
        if (expandOption == null) {
            return;
        }
        List<ExpandItem> expandItems = expandOption.getExpandItems();
        ExpandItem firstExpandItem = expandItems.get(0);
        if (firstExpandItem.isStar()) {
            LevelsExpandOption levelsExpandOption = firstExpandItem.getLevelsOption();
            int expandLevel = 1;
            if (levelsExpandOption != null) {
                expandLevel = levelsExpandOption.getValue();
            }
            List<String> navigationNames = edmEntityType.getNavigationPropertyNames();
            for (String navigationName : navigationNames) {
                EdmNavigationProperty navigationProperty = edmEntityType.getNavigationProperty(navigationName);
                addExpandNavigation(entities, edmEntityType, navigationProperty, expandLevel);
            }
        } else {
            for (ExpandItem expandItem : expandItems) {
                addAllExpandItem(entities, expandItem, edmEntityType);
            }
        }
    }

    private void addExpandNavigation(List<Entity> entities, EdmEntityType edmEntityType, EdmNavigationProperty navigationProperty, int expandLevel) {
    }

    private void addAllExpandItem(List<Entity> entities, ExpandItem expandItem,
                                  EdmEntityType edmEntityType) throws ODataApplicationException {
        EdmNavigationProperty edmNavigationProperty = null;
        LevelsExpandOption levelsExpandOption = expandItem.getLevelsOption();
        int expandLevel = 1;
        if (levelsExpandOption != null) {
            expandLevel = levelsExpandOption.getValue();
        }
        UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
        if (uriResource instanceof UriResourceNavigation) {
            edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
        }
        if (edmNavigationProperty == null) {
            return;
        }
        for (Entity entity : entities) {
            addExpandItem(entity, expandItem, edmEntityType);
        }
    }

    private void addExpandItem(Entity entity, ExpandItem expandItem, EdmEntityType edmEntityType) throws ODataApplicationException {
        EdmNavigationProperty edmNavigationProperty = null;
        LevelsExpandOption levelsExpandOption = expandItem.getLevelsOption();
        int expandLevel = 1;
        if (levelsExpandOption != null) {
            expandLevel = levelsExpandOption.getValue();
        }
        List<UriResource> expandItemPath = expandItem.getResourcePath().getUriResourceParts();
        UriResource uriResource = expandItemPath.get(0);
        if (uriResource instanceof UriResourceNavigation) {
            edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
        }
        if (edmNavigationProperty == null) {
            return;
        }
        String navPropName = edmNavigationProperty.getName();
        Map<String, QueryOption> embeddedQueryOptions = new HashMap<>();
        embeddedQueryOptions.put("orderByOption", expandItem.getOrderByOption());
        embeddedQueryOptions.put("selectOption", expandItem.getSelectOption());
        embeddedQueryOptions.put("searchOption", expandItem.getSearchOption());
        embeddedQueryOptions.put("filterOption", expandItem.getFilterOption());
        if (edmNavigationProperty.isCollection()) { // expand的对象是collection
            ExpandOption nestedExpandOption = expandItem.getExpandOption(); // expand nested in expand
            if (nestedExpandOption == null && expandLevel > 1) {
                ExpandOptionImpl expandOptionImpl = new ExpandOptionImpl();
                LevelsOptionImpl levelsOptionImpl = (LevelsOptionImpl) levelsExpandOption;
                levelsOptionImpl.setValue(expandLevel--);
                expandOptionImpl.addExpandItem(expandItem);
                nestedExpandOption = expandOptionImpl;
            }
            if (nestedExpandOption != null) {
                embeddedQueryOptions.put("expandOption", nestedExpandOption);
            }
            embeddedQueryOptions.put("expandOption", nestedExpandOption);
            expandCollection(entity, edmEntityType, edmNavigationProperty, embeddedQueryOptions);
        } else { // expand对象不是collection
            embeddedQueryOptions.put("expandOption", expandItem.getExpandOption());
            expandNonCollection(entity, edmEntityType, edmNavigationProperty, embeddedQueryOptions);
        } // end expand对象不是collection
    }

    private void expandNonCollection(Entity entity, EdmEntityType edmEntityType,
                                     EdmNavigationProperty edmNavigationProperty,
                                     Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        EntityCollection expandEntityCollection = getExpandData(entity, edmEntityType, edmNavigationProperty, queryOptions);
        if (null != expandEntityCollection && expandEntityCollection.getEntities() != null) {
            Entity expandEntity = expandEntityCollection.getEntities().get(0);
//            expandEntityCollection.setCount(expandEntityCollection.getEntities().size());
            Link link = new Link();
            String navPropName = edmNavigationProperty.getName();
            link.setTitle(navPropName);
            link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
            link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);
            link.setInlineEntity(expandEntity);
            if (entity.getId() != null) {
                String linkHref = entity.getId().toString() + "/" + navPropName;
                link.setHref(linkHref);
            }
            entity.getNavigationLinks().add(link);
        }
   }

    private EntityCollection getExpandData(Entity entity, EdmEntityType edmEntityType,
                                           EdmNavigationProperty edmNavigationProperty,
                                           Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        Map<String, Object> embeddedEdmParams = new HashMap<>();
        embeddedEdmParams.put("edmEntityType", edmEntityType);
        embeddedEdmParams.put("edmNavigationProperty", edmNavigationProperty);
        return findRelatedList((QuarkEntity) entity, edmNavigationProperty, queryOptions);
    }
//    @Override
//    public EntityCollection findRelatedList(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty,
//                                            Map<String, QueryOption> queryOptions) throws ODataApplicationException {
//        EntityCollection entityCollection = new EntityCollection();
//        List<Entity> entities = entityCollection.getEntities();
//        List<GenericEntity> genericEntities = entityService.findRelatedEntity(entity.getGenericEntity(), edmNavigationProperty.getName(), queryOptions);
//        if (genericEntities != null && genericEntities.size() > 0) {
//            EdmEntityType edmEntityType = (EdmEntityType) edmNavigationProperty.getType();
//            List<QuarkEntity> relatedEntities = Util.GenericToEntities(edmEntityType, genericEntities);
//            if (queryOptions != null && queryOptions.get("expandOption") != null) {
//                addExpandOption((ExpandOption) queryOptions.get("expandOption"), relatedEntities, edmEntityType);
//            }
//            entities.addAll(relatedEntities);
//        }
//        return entityCollection;
//    }
    @Override
    public EntityCollection findRelatedList(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty,
                                            Map<String, QueryOption> queryOptions)
            throws ODataApplicationException {
        EntityCollection entityCollection = new EntityCollection();
        String targetEntityName = edmNavigationProperty.getType().getName();
        Map<String, String> mappedProperties = retrieveMappedProperties(edmNavigationProperty);
        List<QuarkEntity> entities = entityService.findRelatedEntity(entity, targetEntityName, mappedProperties, queryOptions);
        //filter、orderby、page
        FilterOption filterOption = queryOptions != null? (FilterOption) queryOptions.get("filterOption"):null;
        OrderByOption orderbyOption = queryOptions != null? (OrderByOption) queryOptions.get("orderByOption"):null;
//        if (filterOption != null || orderbyOption != null) {
//            Util.filterEntityCollection(entityCollection, filterOption, orderbyOption, edmNavigationProperty.getType(),
//                    edmProvider, delegator, dispatcher, userLogin, locale, csdlNavigationProperty.isFilterByDate());
//        }
//        if (Util.isExtraOrderby(orderbyOption, navCsdlEntityType, delegator)) {
//            Util.orderbyEntityCollection(entityCollection, orderbyOption, edmNavigationProperty.getType(), edmProvider);
//        }
        entityCollection.setCount(entities.size());
        entityCollection.getEntities().addAll(entities);
        SkipOption skipOption = queryOptions != null? (SkipOption) queryOptions.get("skipOption"):null;
        TopOption topOption = queryOptions != null? (TopOption) queryOptions.get("topOption"):null;
        Util.pageEntityCollection(entityCollection, skipOption != null? skipOption.getValue():0, topOption != null? topOption.getValue(): EdmConst.MAX_ROWS);
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), entityCollection.getEntities(), edmNavigationProperty.getType());
        }
        return entityCollection;
    }

    private void expandCollection(Entity entity, EdmEntityType edmEntityType,
                                  EdmNavigationProperty edmNavigationProperty,
                                  Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        EntityCollection expandEntityCollection = getExpandData(entity, edmEntityType, edmNavigationProperty, queryOptions);
        String navPropName = edmNavigationProperty.getName();
        Link link = new Link();
        link.setTitle(navPropName);
        link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
        link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);
        link.setInlineEntitySet(expandEntityCollection);
        expandEntityCollection.setCount(expandEntityCollection.getEntities().size());
        if (entity.getId() != null) { // TODO:要检查一下为什么会有id为null的情况
            String linkHref = entity.getId().toString() + "/" + navPropName;
            link.setHref(linkHref);
        }
        entity.getNavigationLinks().add(link);
    }
}
