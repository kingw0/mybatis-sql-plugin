package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.LogicalConditionOp;

import static com.intros.mybatis.plugin.sql.constants.LogicalConditionOp.AND;
import static com.intros.mybatis.plugin.sql.constants.LogicalConditionOp.OR;

public class Binary<S extends Sql<S>> extends Condition<S> {
    private static final Class<Binary> THIS_CLASS = Binary.class;

    private Condition<S> left;

    private LogicalConditionOp op;

    private Condition<S> right;

    protected Binary(Condition<S> left, LogicalConditionOp op, Condition<S> right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public static <S extends Sql<S>> Binary<S> and(Condition<S> left, Condition right) {
        return new Binary<>(left, AND, right);
    }

    public static <S extends Sql<S>> Binary<S> or(Condition<S> left, Condition right) {
        return new Binary<>(left, OR, right);
    }

    @Override
    public S write(S sql) {
        return this.right.write(this.left.write(sql).append(op.op()));
    }
}
