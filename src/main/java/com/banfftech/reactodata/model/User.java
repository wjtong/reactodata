package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class User {
    private String id;
    private String email;
    private String firstName;
    private String createdAt;
    private String password;
    private String verified;
    private String lastName;
    private String features;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public User(String id, String email, String firstName, String createdAt, String password, String verified, String lastName, String features) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.createdAt = createdAt;
        this.password = password;
        this.verified = verified;
        this.lastName = lastName;
        this.features = features;
    }

    public static Multi<User> findAll(PgPool client) {
        return client.query("SELECT id, email, first_name, created_at, password, verified, last_name, features FROM pandas_user ORDER BY email ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(User::from);
    }

    private static User from(Row row) {
        return new User(
            row.getString("id"),
            row.getString("email"),
            row.getString("first_name"),
            row.getString("created_at"),
            row.getString("password"),
            row.getString("verified"),
            row.getString("last_name"),
            row.getString("features")
        );
    }
}
