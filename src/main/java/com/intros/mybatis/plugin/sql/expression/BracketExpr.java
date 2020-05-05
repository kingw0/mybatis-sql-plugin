package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;

import static com.intros.mybatis.plugin.sql.constants.Keywords.CLOSE_BRACKET;
import static com.intros.mybatis.plugin.sql.constants.Keywords.OPEN_BRACKET;

/**
 * @param <S>
 * @author teddy
 */
public class BracketExpr<S extends Sql<S>> extends Expression<S> {
    private static final Class<BracketExpr> THIS_CLASS = BracketExpr.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new BracketExpr((Expression) initArgs[0]));
    }

    private Expression<S> expression;

    protected BracketExpr(Expression<S> expression) {
        this.expression = expression;
    }

    public static <S extends Sql<S>> BracketExpr<S> priority(Expression<S> expression) {
        return instance(THIS_CLASS, expression);
    }

    @Override
    public S write(S sql) {
        return this.expression.write(sql.append(OPEN_BRACKET)).append(CLOSE_BRACKET);
    }
}
