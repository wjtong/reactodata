package com.banfftech.reactodata.service;

import com.banfftech.reactodata.Util;
import com.banfftech.reactodata.odata.QuarkEntity;
import com.banfftech.reactodata.odata.processor.OdataExpressionVisitor;
import io.quarkus.logging.Log;
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
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.api.uri.queryoption.apply.*;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.*;
import java.util.stream.Collectors;

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
        ApplyOption applyOption = (ApplyOption) queryOptions.get("applyOption");
        OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor(edmEntityType);
        String sql = "";
        SqlHolder sqlHolder = new SqlHolder(tableName);
        if (selectOption != null) {
            List<String> selectFields = Util.getSelectOptionFields(selectOption);
            String fields = Util.joinSqlFields(selectFields, tableName);
            sqlHolder.setSelectSql(fields);
        }
        String conditionSql = "";
        String groupBySql = "";
        String joinSql = "";
        sql = sql + tableName + " ";
        try {
            if (filterOption != null) {
                String filterExpressionSql = (String) filterOption.getExpression().accept(expressionVisitor);
                sqlHolder.addCondition(filterExpressionSql);
                joinSql = expressionVisitor.getJoinSql();
                sqlHolder.addJoin(joinSql);
                groupBySql = expressionVisitor.getGroupBySql();
                sqlHolder.addGroupBy(groupBySql);
            }
            if (applyOption != null) {
//                applySql(edmEntityType, applyOption, sqlHolder);
            }
            sql = sql + joinSql + conditionSql + groupBySql;
            Query<RowSet<Row>> query = pgClient.query(sql);
            Log.info(sql);
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

//    private void applySql(EdmEntityType edmEntityType, ApplyOption applyOption, SqlHolder sqlHolder)
//            throws ExpressionVisitException, ODataApplicationException {
//        String applySql = "";
//        List<ApplyItem> applyItems = applyOption.getApplyItems();
//        for (ApplyItem applyItem:applyItems) {
//            if (applyItem instanceof Filter) {
//                Filter filter = (Filter) applyItem;
//
//                FilterOption filterOption = ((Filter) applyItem).getFilterOption();
//                OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor(edmEntityType);
//                String filterExpressionSql = (String) filterOption.getExpression().accept(expressionVisitor);
//                sqlHolder.addCondition(filterExpressionSql);
//                sqlHolder.addJoin(expressionVisitor.getJoinSql());
//                sqlHolder.addGroupBy(expressionVisitor.getGroupBySql());
//            }
//            if (applyItem instanceof GroupBy) {
//                GroupBy groupBy = (GroupBy) applyItem;
//                List<GroupByItem> groupByItems = groupBy.getGroupByItems();
//                for (GroupByItem groupByItem : groupByItems) {
//                    List<UriResource> path = groupByItem.getPath();
//                    if (path.size() == 1) {
//                        String segmentValue = path.get(0).getSegmentValue();
//                        Util.addJoinTable(sqlHolder, null, edmEntityType, segmentValue, null);
//                    } else {
//                        //多段式的子对象字段
//                        //add MemberEntity
//                        UriResource groupByProperty = path.get(path.size() - 1);
//                        path = path.subList(0, path.size() - 1);
//                        List<String> resourceParts = path.stream().map(UriResource::getSegmentValue).collect(Collectors.toList());
//                        EdmEntityType lastEdmEntityType = edmEntityType;
//                        for(String resourcePart:resourceParts) {
//                            lastEdmEntityType = Util.addJoinTable(sqlHolder, null, lastEdmEntityType, resourcePart, null);
//                        }
//                        sqlHolder.addGroupBy(groupByProperty.getSegmentValue());
//                    }
//                }
//                applySql = applySql.substring(0, applySql.length() - 1);
//                applySql = applySql + " group by ";
//                for (GroupByItem groupByItem:groupByItems) {
//                    if (groupByItem instanceof Member) {
//                        Member member = (Member) groupByItem;
//                        String memberName = member.getResourcePath().getUriResourceParts().get(0).toString();
//                        String columnName = Util.javaNameToDbName(memberName);
//                        applySql = applySql + columnName + ",";
//                    }
//                }
//                applySql = applySql.substring(0, applySql.length() - 1);
//            } // end if (applyItem instanceof GroupBy)
//            if (applyItem instanceof Aggregate) {
//                Aggregate aggregate = (Aggregate) applyItem;
//                for (AggregateExpression aggregateExpression:aggregate.getExpressions()) {
//                    //聚合函数类型
//                    AggregateExpression.StandardMethod standardMethod = aggregateExpression.getStandardMethod();
//                    //返回字段别名
//                    String expressionAlias = aggregateExpression.getAlias();
//                    if (isAggregateCount(aggregateExpression)) {
//                        //这里处理aggregate的$count，使用统计主键数量的方式实现，多主键暂不支持
//                        List<String> pkFieldNames = modelEntity.getPkFieldNames();
//                        if (pkFieldNames.size() > 1) {
//                            throw new OfbizODataException("Count queries with multiple primary keys are not supported.");
//                        }
//                        dynamicViewEntity.addAlias(ofbizCsdlEntityType.getName(), expressionAlias, modelEntity.getFirstPkFieldName(), null, false, null, "count");
//                    } else {
//                        if (UtilValidate.isNotEmpty(standardMethod)) {
//                            //expression 字段名称或者子对象名称
//                            String expression = aggregateExpression.getExpression().toString();
//                            expression = expression.substring(1, expression.length() - 1);
//                            if (standardMethod.equals(AggregateExpression.StandardMethod.COUNT_DISTINCT)) {
//                                List<String> relationKeyList = Util.getRelationKey(modelEntity, expression);
//                                if (relationKeyList.size() > 1) {
//                                    throw new OfbizODataException("Multiple field association is not supported.");
//                                }
//                                dynamicViewEntity.addAlias(ofbizCsdlEntityType.getName(), expressionAlias, relationKeyList.get(0), null, false, null, AGGREGATE_MAP.get(standardMethod));
//                            } else {
//                                dynamicViewEntity.addAlias(ofbizCsdlEntityType.getName(), expressionAlias, expression, null, false, null, AGGREGATE_MAP.get(standardMethod));
//                            }
//                        } else {
//                            //default sum
//                            dynamicViewEntity.addAlias(ofbizCsdlEntityType.getName(), expressionAlias, expressionAlias, null, false, null, "sum");
//                        }
//                    }
//                    if (applySelect == null) {
//                        applySelect = new HashSet<>();
//                    }
//                    applySelect.add(expressionAlias);
//                }
//            }
//        }
//        return applySql;
//    }
    private boolean isAggregateCount(AggregateExpression aggregateExpression) {
        List<UriResource> path = aggregateExpression.getPath();
        if (path != null && path.size() > 0) {
            return "$count".equals(path.get(0).getSegmentValue());
        }
        return false;
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
            List<String> selectFields = Util.getSelectOptionFields(selectOption);
            String joinSqlFields = Util.joinSqlFields(selectFields, tableName);
            sql = "select " + joinSqlFields + " from " + tableName + " where ";
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
                sql = sql + " and " + condition + expressionVisitor.getGroupBySql();;
            }
            Log.info(sql);
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
        pgClient.query("DROP TABLE IF EXISTS order_item_fact").execute()
                .flatMap(r -> pgClient.query("CREATE TABLE order_item_fact (id TEXT PRIMARY KEY, order_id TEXT, order_item_seq_id TEXT, " +
                        "product_id TEXT, party_id TEXT, quantity NUMERIC(18,6), amount NUMERIC(18,6), count INTEGER)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO order_item_fact VALUES ('9000', '9000', '0001', '9000', '9000', " +
                        "12, 2400, 1)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO order_item_fact VALUES ('9010', '9000', '0002', '9010', '9000', " +
                        "8, 720, 1)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO order_item_fact VALUES ('9020', '9010', '0001', '9000', '9010', " +
                        "15, 3000, 1)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO order_item_fact VALUES ('9030', '9010', '0002', '9010', '9010', " +
                        "6, 540, 1)").execute())
                .await().indefinitely();
    }
}
