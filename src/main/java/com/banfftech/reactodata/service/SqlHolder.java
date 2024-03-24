package com.banfftech.reactodata.service;

public class SqlHolder {
    String selectSql;
    String tableName;
    String joinSql = null;
    String whereSql = null;
    String groupBySql = null;
    String orderBySql = null;
    String limitSql = null;
    String offsetSql = null;
    String countSql = null;
    String sql;

    public SqlHolder(String tableName) {
        this.tableName = tableName;
        this.selectSql = "*";
    }

    public void addCondition(String condition) {
        if (whereSql == null) {
            whereSql = "where " + condition;
        } else {
            whereSql += " and " + condition;
        }
    }
    public void addJoin(String join) {
        if (joinSql == null) {
            joinSql = join;
        } else {
            joinSql += " " + join;
        }
    }
    public void addGroupBy(String groupBy) {
        if (groupBySql == null) {
            groupBySql = "group by " + groupBy;
        } else {
            groupBySql += " " + groupBy;
        }
    }

    public String getSelectSql() {
        return selectSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getJoinSql() {
        return joinSql;
    }

    public void setJoinSql(String joinSql) {
        this.joinSql = joinSql;
    }

    public String getWhereSql() {
        return whereSql;
    }

    public void setWhereSql(String whereSql) {
        this.whereSql = whereSql;
    }

    public String getGroupBySql() {
        return groupBySql;
    }

    public void setGroupBySql(String groupBySql) {
        this.groupBySql = groupBySql;
    }

    public String getOrderBySql() {
        return orderBySql;
    }

    public void setOrderBySql(String orderBySql) {
        this.orderBySql = orderBySql;
    }

    public String getLimitSql() {
        return limitSql;
    }

    public void setLimitSql(String limitSql) {
        this.limitSql = limitSql;
    }

    public String getOffsetSql() {
        return offsetSql;
    }

    public void setOffsetSql(String offsetSql) {
        this.offsetSql = offsetSql;
    }

    public String getCountSql() {
        return countSql;
    }

    public void setCountSql(String countSql) {
        this.countSql = countSql;
    }

    public String getSql() {
        sql = "select " + selectSql + " from " + tableName + " " + joinSql + " " + whereSql + " " + groupBySql + " " + orderBySql + " " + limitSql + " " + offsetSql + " " + countSql + ";" ;
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
