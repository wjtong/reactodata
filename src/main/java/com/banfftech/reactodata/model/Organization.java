package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class Organization {
    private String id;
    private String name;
    private String url;
    private String isDefault;
    private String settings;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public Organization(String id, String name, String url, String isDefault, String settings) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.isDefault = isDefault;
        this.settings = settings;
    }

    public static Multi<Organization> findAll(PgPool client) {
        return client.query("SELECT id, name, url, is_default, settings FROM organization ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Organization::from);
    }

    private static Organization from(Row row) {
        return new Organization(
            row.getString("id"),
            row.getString("name"),
            row.getString("url"),
            row.getString("is_default"),
            row.getString("settings")
        );
    }
}
