package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.UnaryExpressionOp;

import static com.intros.mybatis.plugin.sql.constants.UnaryExpressionOp.IDENTITY;
import static com.intros.mybatis.plugin.sql.constants.UnaryExpressionOp.NEGATION;

public class Unary<S extends Sql<S>> extends Expression<S> {
    private Expression<S> expr;

    private UnaryExpressionOp op;

    protected Unary(Expression<S> expr, UnaryExpressionOp op) {
        this.expr = expr;
        this.op = op;
    }

    public static <S extends Sql<S>> Unary<S> identity(Expression<S> expr) {
        return new Unary<>(expr, IDENTITY);
    }

    public static <S extends Sql<S>> Unary<S> negation(Expression<S> expr) {
        return new Unary<>(expr, NEGATION);
    }

    @Override
    public S write(S sql) {
        return expr.write(sql.append(op.op()));
    }
}
