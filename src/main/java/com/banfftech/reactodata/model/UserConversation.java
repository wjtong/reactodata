package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class UserConversation {
    private String id;
    private String workspaceId;
    private String userId;
    private String createdAt;
    private Boolean valid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public UserConversation(String id, String workspaceId, String userId, String createdAt, Boolean valid) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.valid = valid;
    }

    public static Multi<UserConversation> findAll(PgPool client) {
        return client.query("SELECT id, workspace_id, user_id, created_at, valid FROM user_conversation ORDER BY created_at ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(UserConversation::from);
    }

    private static UserConversation from(Row row) {
        return new UserConversation(
            row.getString("id"),
            row.getString("workspace_id"),
            row.getString("user_id"),
            row.getString("created_at"),
            row.getBoolean("valid")
        );
    }
}
