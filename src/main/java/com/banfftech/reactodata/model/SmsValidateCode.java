package com.banfftech.reactodata.model;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

public class SmsValidateCode {
    private String telNumber;
    private String captcha;
    private String isValid;
    private String fromDate;
    private String thruDate;

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getThruDate() {
        return thruDate;
    }

    public void setThruDate(String thruDate) {
        this.thruDate = thruDate;
    }

    public SmsValidateCode(String telNumber, String captcha, String isValid, String fromDate, String thruDate) {
        this.telNumber = telNumber;
        this.captcha = captcha;
        this.isValid = isValid;
        this.fromDate = fromDate;
        this.thruDate = thruDate;
    }

    public static Multi<SmsValidateCode> findAll(PgPool client) {
        return client.query("SELECT tel_number, captcha, is_valid, from_date, thru_date FROM sms_validate_code ORDER BY from_date ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(SmsValidateCode::from);
    }

    private static SmsValidateCode from(Row row) {
        return new SmsValidateCode(
            row.getString("tel_number"),
            row.getString("captcha"),
            row.getString("is_valid"),
            row.getString("from_date"),
            row.getString("thru_date")
        );
    }
}
