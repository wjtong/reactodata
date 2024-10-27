package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class OrganizationMembership {
    private String id;
    private String userId;
    private String organizationId;
    private String role;
    private Boolean verified;

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

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public OrganizationMembership(String id, String userId, String organizationId, String role, Boolean verified) {
        this.id = id;
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
        this.verified = verified;
    }

    public static Multi<OrganizationMembership> findAll(PgPool client) {
        return client.query("SELECT id, user_id, organization_id, role, verified FROM organization_membership ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(OrganizationMembership::from);
    }

    private static OrganizationMembership from(Row row) {
        return new OrganizationMembership(
            row.getString("id"),
            row.getString("user_id"),
            row.getString("organization_id"),
            row.getString("role"),
            row.getBoolean("verified")
        );
    }
}
