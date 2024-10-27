package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class Dataset {
    private String id;
    private String name;
    private String tableName;
    private String description;
    private String createdAt;
    private String head;
    private String userId;
    private String organizationId;
    private String connectorId;
    private String fieldDescriptions;
    private String filterableColumns;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getFieldDescriptions() {
        return fieldDescriptions;
    }

    public void setFieldDescriptions(String fieldDescriptions) {
        this.fieldDescriptions = fieldDescriptions;
    }

    public String getFilterableColumns() {
        return filterableColumns;
    }

    public void setFilterableColumns(String filterableColumns) {
        this.filterableColumns = filterableColumns;
    }

    public Dataset(String id, String name, String tableName, String description, String createdAt, String head, String userId, String organizationId, String connectorId, String fieldDescriptions, String filterableColumns) {
        this.id = id;
        this.name = name;
        this.tableName = tableName;
        this.description = description;
        this.createdAt = createdAt;
        this.head = head;
        this.userId = userId;
        this.organizationId = organizationId;
        this.connectorId = connectorId;
        this.fieldDescriptions = fieldDescriptions;
        this.filterableColumns = filterableColumns;
    }

    public static Multi<Dataset> findAll(PgPool client) {
        return client.query("SELECT id, name, table_name, description, created_at, head, user_id, organization_id, connector_id, field_descriptions, filterable_columns FROM dataset ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Dataset::from);
    }

    private static Dataset from(Row row) {
        return new Dataset(
            row.getString("id"),
            row.getString("name"),
            row.getString("table_name"),
            row.getString("description"),
            row.getString("created_at"),
            row.getString("head"),
            row.getString("user_id"),
            row.getString("organization_id"),
            row.getString("connector_id"),
            row.getString("field_descriptions"),
            row.getString("filterable_columns")
        );
    }
}
