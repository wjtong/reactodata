package com.banfftech.reactodata.service;

import com.banfftech.reactodata.Util;
import com.banfftech.reactodata.odata.QuarkEntity;
import com.banfftech.reactodata.odata.processor.OdataExpressionVisitor;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Query;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class EntityServiceImpl implements EntityService {
    @Inject
    PgPool pgClient;
    @Inject
    @ConfigProperty(name = "reactodata.schema.create", defaultValue = "true")
    boolean schemaCreate;

    @Override
    public List<QuarkEntity> findEntity(EdmEntityType edmEntityType, Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        String tableName = Util.javaNameToDbName(edmEntityType.getName());
        FilterOption filterOption = (FilterOption) queryOptions.get("filterOption");
        SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
        OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor(edmEntityType);
        String sql;
        if (selectOption == null) {
            sql = "select " + tableName + ".* from ";
        } else {
            // TODO: selectOption is not implemented yet
            sql = "select * from ";
        }
        String condition = null;
        try {
            if (filterOption != null) {
                condition = (String) filterOption.getExpression().accept(expressionVisitor);
                sql = sql + expressionVisitor.getFromSql();
                sql = sql + " where " + condition + expressionVisitor.getGroupBySql();
            } else {
                sql = sql + tableName;
            }
            Query<RowSet<Row>> query = pgClient.query(sql);
            System.out.println(sql);
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
    public List<QuarkEntity> findRelatedEntity(QuarkEntity entity, EdmEntityType targetEdmEntityType, Map<String, String> mappedProperties, Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        FilterOption filterOption = (FilterOption) queryOptions.get("filterOption");
        SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
        String tableName = Util.javaNameToDbName(targetEdmEntityType.getName());
        OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor(targetEdmEntityType);
        String sql;
        if (selectOption == null) {
            sql = "select * from " + tableName + " where ";
        } else {
            // TODO: selectOption is not implemented yet
            sql = "select * from " + tableName + " where ";
        }
        Set<Map.Entry<String, String>> entrySet = mappedProperties.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            String column = Util.javaNameToDbName(value);
            sql = sql + column + " = '" + entity.getProperty(key).getValue().toString() + "'";
            if (iterator.hasNext()) {
                sql = sql + " and ";
            }
        }
        String condition = null;
        try {
            if (filterOption != null) {
                condition = (String) filterOption.getExpression().accept(expressionVisitor);
                sql = sql + " and " + condition;
            }
            System.out.println(sql);
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
    public QuarkEntity findEntityById(String entityName, String id, Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
        String tableName = Util.javaNameToDbName(entityName);
        String sql;
        if (selectOption == null) {
            sql = "select * from " + tableName + " where id = '" + id + "'";
        } else {
            // TODO: selectOption is not implemented yet
            sql = "select * from " + tableName + " where id = '" + id + "'";
        }
        Query<RowSet<Row>> query = pgClient.query(sql);
        Uni<QuarkEntity> quarkEntityUni = query.execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? QuarkEntity.from(iterator.next()) : null);
        return quarkEntityUni.await().indefinitely();
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
                .flatMap(r -> pgClient.query("INSERT INTO party VALUES ('9030', 'Zhang San', 'PERSON', 'ENABLED')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party VALUES ('9040', 'Wang Qiang', 'PERSON', 'ENABLED')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party VALUES ('9050', 'Li Si', 'PERSON', 'ENABLED')").execute())
                .await().indefinitely();
        pgClient.query("DROP TABLE IF EXISTS person").execute()
                .flatMap(r -> pgClient.query("CREATE TABLE person (id TEXT PRIMARY KEY, last_name TEXT, first_name TEXT, party_id TEXT NOT NULL)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO person VALUES ('9000', 'Zhang', 'San', '9030')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO person VALUES ('9010', 'Wang', 'Qiang', '9040')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO person VALUES ('9020', 'Li', 'Si', '9050')").execute())
                .await().indefinitely();
        pgClient.query("DROP TABLE IF EXISTS party_role").execute()
                .flatMap(r -> pgClient.query("CREATE TABLE party_role (id TEXT PRIMARY KEY, party_id TEXT NOT NULL, role_type_id TEXT NOT NULL)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9000', '9000', 'CARRIER')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9010', '9000', 'SUPPLIER')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9020', '9000', 'ACCOUNT')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9030', '9030', 'EMAIL_ADMIN')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9040', '9030', 'SALES_REP')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9050', '9030', 'SHIPMENT_CLERK')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO party_role VALUES ('9060', '9040', 'SHIPMENT_CLERK')").execute())
                .await().indefinitely();
    }
}
