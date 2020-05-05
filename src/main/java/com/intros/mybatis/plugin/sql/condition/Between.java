package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.expression.Expression;

import static com.intros.mybatis.plugin.sql.constants.Keywords.KW_AND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.KW_BETWEEN;

/**
 * Between sql clause
 *
 * @param <S>
 * @author teddy
 */
public class Between<S extends Sql<S>> extends Condition<S> {
    private static final Class<Between> THIS_CLASS = Between.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Between((Expression) initArgs[0], (Expression) initArgs[1], (Expression) initArgs[2]));
    }

    private Expression<S> expr;

    private Expression<S> lower;

    private Expression<S> upper;

    protected Between(Expression<S> expr, Expression<S> lower, Expression<S> upper) {
        this.expr = expr;
        this.lower = lower;
        this.upper = upper;
    }

    public static <S extends Sql<S>> Between<S> between(Expression<S> expr, Expression<S> lower, Expression<S> upper) {
        return instance(THIS_CLASS, expr, lower, upper);
    }

    @Override
    public S write(S sql) {
        expr.write(sql).append(KW_BETWEEN);
        return upper.write(lower.write(sql).append(KW_AND));
    }
}
