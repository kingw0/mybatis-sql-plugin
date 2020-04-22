package com.intros.mybatis.plugin.sql.constants;

public enum BinaryExpressionOp {
    ADDITION(" + "), SUBTRACTION(" - "), MULTIPLICATION(" * "), DIVISION(" / ");

    private String op;

    BinaryExpressionOp(String op) {
        this.op = op;
    }

    public String op() {
        return op;
    }
}
