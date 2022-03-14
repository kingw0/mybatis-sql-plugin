package cn.intros.mybatis.plugin.sql.constants;

public enum LogicalConditionOp {
    NOT(" NOT "), AND(" AND "), OR(" OR ");

    private String op;

    LogicalConditionOp(String op) {
        this.op = op;
    }

    public String op() {
        return op;
    }
}
