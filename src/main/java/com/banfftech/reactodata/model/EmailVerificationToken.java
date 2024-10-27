package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class EmailVerificationToken {
    private String tokenId;
    private String userLoginId;
    private String token;
    private String expirationDate;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public EmailVerificationToken(String tokenId, String userLoginId, String token, String expirationDate) {
        this.tokenId = tokenId;
        this.userLoginId = userLoginId;
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public static Multi<EmailVerificationToken> findAll(PgPool client) {
        return client.query("SELECT token_id, user_login_id, token, expiration_date FROM email_verification_token ORDER BY expiration_date ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(EmailVerificationToken::from);
    }

    private static EmailVerificationToken from(Row row) {
        return new EmailVerificationToken(
            row.getString("token_id"),
            row.getString("user_login_id"),
            row.getString("token"),
            row.getString("expiration_date")
        );
    }
}
