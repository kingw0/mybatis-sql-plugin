package io.github.kingw0.mybatis.plugin.sql.condition;

import io.github.kingw0.mybatis.plugin.sql.Joiner;
import io.github.kingw0.mybatis.plugin.sql.Select;
import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.expression.Expression;

import static io.github.kingw0.mybatis.plugin.sql.constants.Keywords.*;

/**
 * In sql clause
 *
 * @param <S>
 * @author teddy
 */
public class In<S extends Sql<S>> extends Condition<S> {
    private Expression<S> expr;

    private Expression<S>[] values;

    private Select sql;

    protected In(Expression<S> expr, Expression<S>[] values) {
        this.expr = expr;
        this.values = values;
    }

    protected In(Expression<S> expr, Select sql) {
        this.expr = expr;
        this.sql = sql;
    }

    public static <S extends Sql<S>> In<S> in(Expression<S> expr, Expression<S>... values) {
        return new In(expr, values);
    }

    public static <S extends Sql<S>> In<S> in(Expression<S> expr, Select sql) {
        return new In(expr, sql);
    }

    @Override
    public S write(S sql) {
        if (this.sql == null) {
            return Joiner.join(this.expr.write(sql).append(KW_IN), COMMA_WITH_SPACE, OPEN_BRACKET, CLOSE_BRACKET,
                this.values);
        } else {
            return this.expr.write(sql).append(KW_IN).append(OPEN_BRACKET).append(this.sql).append(CLOSE_BRACKET);
        }
    }
}
