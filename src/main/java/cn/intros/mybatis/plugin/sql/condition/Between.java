package cn.intros.mybatis.plugin.sql.condition;

import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.expression.Expression;

import static cn.intros.mybatis.plugin.sql.constants.Keywords.KW_AND;
import static cn.intros.mybatis.plugin.sql.constants.Keywords.KW_BETWEEN;

/**
 * Between sql clause
 *
 * @param <S>
 * @author teddy
 */
public class Between<S extends Sql<S>> extends Condition<S> {
    private Expression<S> expr;

    private Expression<S> lower;

    private Expression<S> upper;

    protected Between(Expression<S> expr, Expression<S> lower, Expression<S> upper) {
        this.expr = expr;
        this.lower = lower;
        this.upper = upper;
    }

    public static <S extends Sql<S>> Between<S> between(Expression<S> expr, Expression<S> lower, Expression<S> upper) {
        return new Between<>(expr, lower, upper);
    }

    @Override
    public S write(S sql) {
        expr.write(sql).append(KW_BETWEEN);
        return upper.write(lower.write(sql).append(KW_AND));
    }
}
