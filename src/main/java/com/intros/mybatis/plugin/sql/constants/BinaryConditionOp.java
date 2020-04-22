package com.intros.mybatis.plugin.sql.constants;

public enum BinaryConditionOp {
    EQ(" = "), NEQ(" <> "), GT(" > "), LT(" < "), GTE(" >= "), LTE(" <= "), LIKE(" LIKE ");

    private String op;

    BinaryConditionOp(String op) {
        this.op = op;
    }

    public String op() {
        return op;
    }
}
