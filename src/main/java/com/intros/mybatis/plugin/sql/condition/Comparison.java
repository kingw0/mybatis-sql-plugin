package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.BinaryConditionOp;
import com.intros.mybatis.plugin.sql.expression.Expression;

public class Comparison<S extends Sql<S>> extends Condition<S> {
    private static final Class<Comparison> THIS_CLASS = Comparison.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Comparison((Expression) initArgs[0], (Expression) initArgs[1], (BinaryConditionOp) initArgs[2]));
    }

    private Expression<S> left;

    private Expression<S> right;

    private BinaryConditionOp op;

    protected Comparison(Expression<S> left, Expression<S> right, BinaryConditionOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public static <S extends Sql<S>> Comparison eq(Expression left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.EQ);
    }

    public static <S extends Sql<S>> Comparison gt(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.GT);
    }

    public static <S extends Sql<S>> Comparison gte(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.GTE);
    }

    public static <S extends Sql<S>> Comparison lt(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.LT);
    }

    public static <S extends Sql<S>> Comparison lte(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.LTE);
    }

    public static <S extends Sql<S>> Comparison neq(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.NEQ);
    }

    public static <S extends Sql<S>> Comparison like(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, right, BinaryConditionOp.LIKE);
    }

    @Override
    public S write(S sql) {
        return right.write(left.write(sql).append(op.op()));
    }
}
