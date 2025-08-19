package io.github.kingw0.mybatis.plugin.sql.expression;

import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.constants.UnaryExpressionOp;

import static io.github.kingw0.mybatis.plugin.sql.constants.UnaryExpressionOp.*;

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

    public static <S extends Sql<S>> Unary<S> distinct(Expression<S> expr) {
        return new Unary<>(expr, DISTINCT);
    }

    @Override
    public S write(S sql) {
        return expr.write(sql.append(op.op()));
    }
}
