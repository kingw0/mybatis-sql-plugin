package cn.intros.mybatis.plugin.sql.condition;

import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.constants.BinaryConditionOp;
import cn.intros.mybatis.plugin.sql.expression.Expression;

import static cn.intros.mybatis.plugin.sql.constants.BinaryConditionOp.*;

public class Comparison<S extends Sql<S>> extends Condition<S> {
    private Expression<S> left;

    private Expression<S> right;

    private BinaryConditionOp op;

    protected Comparison(Expression<S> left, Expression<S> right, BinaryConditionOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public static <S extends Sql<S>> Comparison eq(Expression left, Expression<S> right) {
        return new Comparison(left, right, EQ);
    }

    public static <S extends Sql<S>> Comparison gt(Expression<S> left, Expression<S> right) {
        return new Comparison(left, right, GT);
    }

    public static <S extends Sql<S>> Comparison gte(Expression<S> left, Expression<S> right) {
        return new Comparison(left, right, GTE);
    }

    public static <S extends Sql<S>> Comparison lt(Expression<S> left, Expression<S> right) {
        return new Comparison(left, right, LT);
    }

    public static <S extends Sql<S>> Comparison lte(Expression<S> left, Expression<S> right) {
        return new Comparison(left, right, LTE);
    }

    public static <S extends Sql<S>> Comparison neq(Expression<S> left, Expression<S> right) {
        return new Comparison(left, right, NEQ);
    }

    public static <S extends Sql<S>> Comparison like(Expression<S> left, Expression<S> right) {
        return new Comparison(left, right, LIKE);
    }

    @Override
    public S write(S sql) {
        return right.write(left.write(sql).append(op.op()));
    }
}
