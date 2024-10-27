package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class Connector {
    private String id;
    private String type;
    private String config;
    private String createdAt;
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Connector(String id, String type, String config, String createdAt, String userId) {
        this.id = id;
        this.type = type;
        this.config = config;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public static Multi<Connector> findAll(PgPool client) {
        return client.query("SELECT id, type, config, created_at, user_id FROM connector ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Connector::from);
    }

    private static Connector from(Row row) {
        return new Connector(
            row.getString("id"),
            row.getString("type"),
            row.getString("config"),
            row.getString("created_at"),
            row.getString("user_id")
        );
    }
}
