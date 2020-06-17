package com.intros.mybatis.plugin.sql.constants;

public enum UnaryExpressionOp {
    IDENTITY(" +"), NEGATION(" -");

    private String op;

    UnaryExpressionOp(String op) {
        this.op = op;
    }

    public String op() {
        return op;
    }
}
