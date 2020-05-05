package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Joiner;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.expression.Expression;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * In sql clause
 *
 * @param <S>
 * @author teddy
 */
public class In<S extends Sql<S>> extends Condition<S> {
    private static final Class<In> THIS_CLASS = In.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new In((Expression) initArgs[0], (Expression[]) initArgs[1]));
    }

    private Expression<S> expr;

    private Expression<S>[] values;

    protected In(Expression<S> expr, Expression<S>[] values) {
        this.expr = expr;
        this.values = values;
    }

    public static <S extends Sql<S>> In<S> in(Expression<S> expr, Expression<S>... values) {
        return instance(THIS_CLASS, expr, values);
    }

    @Override
    public S write(S sql) {
        return Joiner.join(this.expr.write(sql).append(KW_IN), COMMA_WITH_SPACE, OPEN_BRACKET, CLOSE_BRACKET, this.values);
    }
}
