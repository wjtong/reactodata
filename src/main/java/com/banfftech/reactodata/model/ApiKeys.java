package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class ApiKeys {
    private String id;
    private String organizationId;
    private String apiKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ApiKeys(String id, String organizationId, String apiKey) {
        this.id = id;
        this.organizationId = organizationId;
        this.apiKey = apiKey;
    }

    public static Multi<ApiKeys> findAll(PgPool client) {
        return client.query("SELECT id, organization_id, api_key FROM api_keys ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(ApiKeys::from);
    }

    private static ApiKeys from(Row row) {
        return new ApiKeys(
            row.getString("id"),
            row.getString("organization_id"),
            row.getString("api_key")
        );
    }
}
