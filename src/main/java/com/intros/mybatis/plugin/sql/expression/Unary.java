package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.SQL;
import com.intros.mybatis.plugin.sql.constants.UnaryExpressionOp;

public class Unary<S extends SQL<S>> extends Expression<S> {
    private static final Class<Unary> THIS_CLASS = Unary.class;

    static {
        registerFactory(THIS_CLASS, (initArgs -> new Unary((Expression) initArgs[0], (UnaryExpressionOp) initArgs[1])));
    }

    private Expression<S> expr;

    private UnaryExpressionOp op;

    protected Unary(Expression<S> expr, UnaryExpressionOp op) {
        this.expr = expr;
        this.op = op;
    }

    public static <S extends SQL<S>> Unary<S> identity(Expression<S> expr) {
        return instance(THIS_CLASS, expr, UnaryExpressionOp.IDENTITY);
    }

    public static <S extends SQL<S>> Unary<S> negation(Expression<S> expr) {
        return instance(THIS_CLASS, expr, UnaryExpressionOp.NEGATION);
    }

    @Override
    public S write(S sql) {
        return expr.write(sql.append(op.op()));
    }
}
