package io.github.kingw0.mybatis.plugin.sql.expression;

import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.SqlWriter;
import io.github.kingw0.mybatis.plugin.sql.constants.Keywords;

import java.util.ArrayList;
import java.util.List;

/**
 * Case expression
 *
 * @author teddy
 */
public class Case<S extends Sql<S>> extends Expression<S> {
    private List<SqlWriter<S>> exprs = new ArrayList<>(4);

    private List<Expression<S>> values = new ArrayList<>(4);

    private Expression<S> expr;

    private Expression<S> defaultValue;

    /**
     * for search case
     */
    protected Case() {
    }

    /**
     * for simple case
     *
     * @param expr
     */
    protected Case(Expression expr) {
        this.expr = expr;
    }

    public static <S extends Sql<S>> Case<S> cas(Expression<S> expr) {
        return new Case<>(expr);
    }

    public static <S extends Sql<S>> Case<S> cas() {
        return new Case<>();
    }

    public Case when(Expression<S> expr, Expression<S> value) {
        exprs.add(expr);
        values.add(value);
        return this;
    }

    public Case el(Expression<S> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public S write(S sql) {
        sql.append(Keywords.KW_CASE);

        if (expr != null) {
            expr.write(sql);
        }

        for (int i = 0, len = exprs.size(); i < len; i++) {
            values.get(i).write(exprs.get(i).write(sql.append(Keywords.KW_WHEN)).append(Keywords.KW_THEN));
        }

        if (defaultValue != null) {
            defaultValue.write(sql.append(Keywords.KW_ELSE));
        }

        sql.append(Keywords.KW_END);

        return sql;
    }
}
