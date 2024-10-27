package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class UserSpace {
    private String workspaceId;
    private String userId;

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

    public UserSpace(String workspaceId, String userId) {
        this.workspaceId = workspaceId;
        this.userId = userId;
    }

    public static Multi<UserSpace> findAll(PgPool client) {
        return client.query("SELECT workspace_id, user_id FROM user_space ORDER BY workspace_id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(UserSpace::from);
    }

    private static UserSpace from(Row row) {
        return new UserSpace(
            row.getString("workspace_id"),
            row.getString("user_id")
        );
    }
}
