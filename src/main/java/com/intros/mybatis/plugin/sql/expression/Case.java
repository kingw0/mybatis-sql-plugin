package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.SqlWriter;

import java.util.ArrayList;
import java.util.List;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

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
        sql.append(KW_CASE);

        if (expr != null) {
            expr.write(sql);
        }

        for (int i = 0, len = exprs.size(); i < len; i++) {
            values.get(i).write(exprs.get(i).write(sql.append(KW_WHEN)).append(KW_THEN));
        }

        if (defaultValue != null) {
            defaultValue.write(sql.append(KW_ELSE));
        }

        sql.append(KW_END);

        return sql;
    }
}
