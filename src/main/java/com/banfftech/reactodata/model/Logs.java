package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class Logs {
    private String id;
    private String userId;
    private String apiKey;
    private String createdAt;
    private String query;
    private Double executionTime;
    private Boolean success;
    private String jsonLog;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Double executionTime) {
        this.executionTime = executionTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getJsonLog() {
        return jsonLog;
    }

    public void setJsonLog(String jsonLog) {
        this.jsonLog = jsonLog;
    }

    public Logs(String id, String userId, String apiKey, String createdAt, String query, Double executionTime, Boolean success, String jsonLog) {
        this.id = id;
        this.userId = userId;
        this.apiKey = apiKey;
        this.createdAt = createdAt;
        this.query = query;
        this.executionTime = executionTime;
        this.success = success;
        this.jsonLog = jsonLog;
    }

    public static Multi<Logs> findAll(PgPool client) {
        return client.query("SELECT id, user_id, api_key, created_at, query, execution_time, success, json_log FROM logs ORDER BY created_at ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Logs::from);
    }

    private static Logs from(Row row) {
        return new Logs(
            row.getString("id"),
            row.getString("user_id"),
            row.getString("api_key"),
            row.getString("created_at"),
            row.getString("query"),
            row.getDouble("execution_time"),
            row.getBoolean("success"),
            row.getString("json_log")
        );
    }
}
