package cn.intros.mybatis.plugin.sql.condition;

import cn.intros.mybatis.plugin.sql.Joiner;
import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.expression.Expression;

import static cn.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * In sql clause
 *
 * @param <S>
 * @author teddy
 */
public class In<S extends Sql<S>> extends Condition<S> {
    private Expression<S> expr;

    private Expression<S>[] values;

    protected In(Expression<S> expr, Expression<S>[] values) {
        this.expr = expr;
        this.values = values;
    }

    public static <S extends Sql<S>> In<S> in(Expression<S> expr, Expression<S>... values) {
        return new In(expr, values);
    }

    @Override
    public S write(S sql) {
        return Joiner.join(this.expr.write(sql).append(KW_IN), COMMA_WITH_SPACE, OPEN_BRACKET, CLOSE_BRACKET, this.values);
    }
}
