package io.github.kingw0.mybatis.plugin.sql.constants;

public enum UnaryExpressionOp {
    IDENTITY(" +"), NEGATION(" -"), DISTINCT(" distinct ");

    private String op;

    UnaryExpressionOp(String op) {
        this.op = op;
    }

    public String op() {
        return op;
    }
}
