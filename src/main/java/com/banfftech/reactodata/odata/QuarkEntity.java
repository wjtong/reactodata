package com.banfftech.reactodata.odata;

import io.vertx.mutiny.sqlclient.Row;
import org.apache.olingo.commons.api.data.Entity;

public class QuarkEntity extends Entity {
    public static QuarkEntity from(Row row) {
        return new QuarkEntity();
    }
}
