package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class DatasetSpace {
    private String id;
    private String datasetId;
    private String workspaceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public DatasetSpace(String id, String datasetId, String workspaceId) {
        this.id = id;
        this.datasetId = datasetId;
        this.workspaceId = workspaceId;
    }

    public static Multi<DatasetSpace> findAll(PgPool client) {
        return client.query("SELECT id, dataset_id, workspace_id FROM dataset_space ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(DatasetSpace::from);
    }

    private static DatasetSpace from(Row row) {
        return new DatasetSpace(
            row.getString("id"),
            row.getString("dataset_id"),
            row.getString("workspace_id")
        );
    }
}
