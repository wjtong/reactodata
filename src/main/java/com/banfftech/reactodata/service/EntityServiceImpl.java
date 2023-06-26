package com.banfftech.reactodata.service;

import com.banfftech.reactodata.edmconfig.EdmConst;
import com.banfftech.reactodata.model.Fruit;
import com.banfftech.reactodata.odata.QuarkEntity;
import com.banfftech.reactodata.odata.processor.OdataExpressionVisitor;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Query;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class EntityServiceImpl implements EntityService {
    @Inject
    PgPool pgClient;
    @Inject
    @ConfigProperty(name = "reactodata.schema.create", defaultValue = "true")
    boolean schemaCreate;

    @Override
    public List<QuarkEntity> findEntity(String tableName, Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        FilterOption filterOption = (FilterOption) queryOptions.get("filterOption");
        SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
        OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor();
        String sql;
        if (selectOption == null) {
            sql = "select * from " + tableName;
        } else {
            // TODO: selectOption is not implemented yet
            sql = "select * from " + tableName;
        }
        String condition = null;
        List<QuarkEntity> result = new ArrayList<>();
        try {
            if (filterOption != null) {
                condition = (String) filterOption.getExpression().accept(expressionVisitor);
                sql = sql + " where " + condition;
            }
            Query<RowSet<Row>> query = pgClient.query(sql);
            Multi<QuarkEntity> quarkEntityMulti = query.execute()
                    .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                    .onItem().transform(QuarkEntity::from);
            return quarkEntityMulti.collect().asList().await().indefinitely();
        } catch (ExpressionVisitException e) {
            e.printStackTrace();
            throw new ODataApplicationException(e.getMessage(),
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
    }

    @Override
    public List<QuarkEntity> findRelatedEntity(QuarkEntity entity, String navigationName, Map<String, QueryOption> queryOptions) throws ODataApplicationException {
//        Field[] fields = entity.getClass().getDeclaredFields();
//        for (Field field:fields) {
//            String fieldName = field.getName();
//            if (!fieldName.equals(navigationName)) {
//                continue;
//            }
//            field.setAccessible(true);
//            Object fieldValue = null;
//            try {
//                fieldValue =field.get(entity);
//            } catch (IllegalAccessException e) {
//                throw new ODataApplicationException(e.getMessage(),
//                        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
//            }
//            if (fieldValue == null) {
//                return null;
//            }
//            if (fieldValue instanceof List) {
//                return (List<GenericEntity>) fieldValue;
//            } else {
//                return List.of((GenericEntity) fieldValue);
//            }
//        }
        return null;
    }

    @Override
    public QuarkEntity findEntityById(String entityName, String id) throws ODataApplicationException {
//        Class<?> objectClass = null;
//        GenericEntity genericEntity = null;
//        try {
//            String packageEntityName = EdmConst.ENTITY_PACKAGE + "." + entityName;
//            objectClass = Class.forName(packageEntityName);
//            Method method = objectClass.getMethod("findById", Object.class);
//            genericEntity = (GenericEntity) method.invoke(objectClass, id);
//        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//        return genericEntity;
        return null;
    }
    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        pgClient.query("DROP TABLE IF EXISTS fruits").execute()
                .flatMap(r -> pgClient.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO fruits (name) VALUES ('Orange')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO fruits (name) VALUES ('Pear')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO fruits (name) VALUES ('Apple')").execute())
                .await().indefinitely();
        pgClient.query("DROP TABLE IF EXISTS party").execute()
                .flatMap(r -> pgClient.query("CREATE TABLE party (id TEXT PRIMARY KEY, party_name TEXT NOT NULL, party_type_id TEXT NOT NULL, status_id TEXT NOT NULL)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party VALUES ('9000', 'Orange Co.', 'PARTY_GROUP', 'ENABLED')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party VALUES ('9010', 'Pearl Co.', 'PARTY_GROUP', 'ENABLED')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party VALUES ('9020', 'Apple Co.', 'PARTY_GROUP', 'ENABLED')").execute())
                .await().indefinitely();
    }
}
