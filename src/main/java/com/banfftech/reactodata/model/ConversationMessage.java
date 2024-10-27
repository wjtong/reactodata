package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class ConversationMessage {
    private String id;
    private String conversationId;
    private String createdAt;
    private String query;
    private String response;
    private String codeGenerated;
    private String label;
    private String logId;
    private String settings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getCodeGenerated() {
        return codeGenerated;
    }

    public void setCodeGenerated(String codeGenerated) {
        this.codeGenerated = codeGenerated;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public ConversationMessage(String id, String conversationId, String createdAt, String query, String response, String codeGenerated, String label, String logId, String settings) {
        this.id = id;
        this.conversationId = conversationId;
        this.createdAt = createdAt;
        this.query = query;
        this.response = response;
        this.codeGenerated = codeGenerated;
        this.label = label;
        this.logId = logId;
        this.settings = settings;
    }

    public static Multi<ConversationMessage> findAll(PgPool client) {
        return client.query("SELECT id, conversation_id, created_at, query, response, code_generated, label, log_id, settings FROM conversation_message ORDER BY created_at ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(ConversationMessage::from);
    }

    private static ConversationMessage from(Row row) {
        return new ConversationMessage(
            row.getString("id"),
            row.getString("conversation_id"),
            row.getString("created_at"),
            row.getString("query"),
            row.getString("response"),
            row.getString("code_generated"),
            row.getString("label"),
            row.getString("log_id"),
            row.getString("settings")
        );
    }
}
