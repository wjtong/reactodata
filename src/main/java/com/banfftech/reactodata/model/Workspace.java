package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class Workspace {
    private String id;
    private String name;
    private String userId;
    private String organizationId;
    private String slug;
    private String createdAt;

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Workspace(String id, String name, String userId, String organizationId, String slug, String createdAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.organizationId = organizationId;
        this.slug = slug;
        this.createdAt = createdAt;
    }

    public static Multi<Workspace> findAll(PgPool client) {
        return client.query("SELECT id, name, user_id, organization_id, slug, created_at FROM workspace ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Workspace::from);
    }

    private static Workspace from(Row row) {
        return new Workspace(
            row.getString("id"),
            row.getString("name"),
            row.getString("user_id"),
            row.getString("organization_id"),
            row.getString("slug"),
            row.getString("created_at")
        );
    }
}
