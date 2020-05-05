package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.LogicalConditionOp;

public class Binary<S extends Sql<S>> extends Condition<S> {
    private static final Class<Binary> THIS_CLASS = Binary.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Binary((Condition) initArgs[0], (LogicalConditionOp) initArgs[1], (Condition) initArgs[2]));
    }

    private Condition<S> left;

    private LogicalConditionOp op;

    private Condition<S> right;

    protected Binary(Condition<S> left, LogicalConditionOp op, Condition<S> right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public static <S extends Sql<S>> Binary<S> and(Condition<S> left, Condition right) {
        return instance(THIS_CLASS, left, LogicalConditionOp.AND, right);
    }

    public static <S extends Sql<S>> Binary<S> or(Condition<S> left, Condition right) {
        return instance(THIS_CLASS, left, LogicalConditionOp.OR, right);
    }

    @Override
    public S write(S sql) {
        return this.right.write(this.left.write(sql).append(op.op()));
    }
}
