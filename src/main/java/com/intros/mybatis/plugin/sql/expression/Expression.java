package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Joiner;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.SqlPart;
import com.intros.mybatis.plugin.sql.condition.Between;
import com.intros.mybatis.plugin.sql.condition.Comparison;
import com.intros.mybatis.plugin.sql.condition.In;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;
import static com.intros.mybatis.plugin.sql.expression.Column.column;
import static com.intros.mybatis.plugin.sql.expression.Literal.number;
import static com.intros.mybatis.plugin.sql.expression.Literal.text;

/**
 * Sql expression
 *
 * @author teddy
 */
public abstract class Expression<S extends Sql<S>> extends SqlPart<S> {
    /**
     * @param expression
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Expression<S> expression(String expression) {
        return new Expression<S>() {
            @Override
            public S write(S sql) {
                return sql.append(expression);
            }
        };
    }

    /**
     * Function
     *
     * @param func
     * @param expressions
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Expression<S> func(String func, Expression<S>... expressions) {
        return new Expression<S>() {
            @Override
            public S write(S sql) {
                sql.append(func).append(OPEN_BRACKET);

                if (expressions != null && expressions.length > 0) {
                    Joiner.join(sql, COMMA_WITH_SPACE, expressions);
                }

                return sql.append(CLOSE_BRACKET);
            }
        };
    }

    /**
     * Expression surround by bracket
     *
     * @param expression
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Expression<S> bracket(final Expression<S> expression) {
        return new Expression<S>() {
            @Override
            public S write(S sql) {
                return expression.write(sql.append(OPEN_BRACKET)).append(CLOSE_BRACKET);
            }
        };
    }

    /**
     * Select expression surround by bracket
     *
     * @param select
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Expression<S> bracket(final Select select) {
        return new Expression<S>() {
            @Override
            public S write(S sql) {
                return sql.append(OPEN_BRACKET).append(select).append(CLOSE_BRACKET);
            }
        };
    }

    /**
     * @param expr
     * @return
     */
    public Expression<S> add(Expression<S> expr) {
        return Binary.add(this, expr);
    }

    /**
     * @param column
     * @return
     */
    public Expression<S> add(String column) {
        return Binary.add(this, column(column));
    }

    /**
     * @param number
     * @return
     */
    public Expression<S> add(Number number) {
        return Binary.add(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Expression<S> sub(Expression<S> expr) {
        return Binary.sub(this, expr);
    }

    /**
     * @param column
     * @return
     */
    public Expression<S> sub(String column) {
        return Binary.sub(this, column(column));
    }

    /**
     * @param number
     * @return
     */
    public Expression<S> sub(Number number) {
        return Binary.sub(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Expression<S> mul(Expression<S> expr) {
        return Binary.mul(this, expr);
    }

    /**
     * @param column
     * @return
     */
    public Expression<S> mul(String column) {
        return Binary.mul(this, column(column));
    }

    /**
     * @param number
     * @return
     */
    public Expression<S> mul(Number number) {
        return Binary.mul(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Expression<S> div(Expression<S> expr) {
        return Binary.div(this, expr);
    }

    /**
     * @param column
     * @return
     */
    public Expression<S> div(String column) {
        return Binary.div(this, column(column));
    }

    /**
     * @param number
     * @return
     */
    public Expression<S> div(Number number) {
        return Binary.div(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> eq(Expression<S> expr) {
        return Comparison.eq(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> eq(String text) {
        return Comparison.eq(this, text(text));
    }

    /**
     * @param number
     * @return
     */
    public Comparison<S> eq(Number number) {
        return Comparison.eq(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> gt(Expression<S> expr) {
        return Comparison.gt(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> gt(String text) {
        return Comparison.gt(this, text(text));
    }

    /**
     * @param number
     * @return
     */
    public Comparison<S> gt(Number number) {
        return Comparison.gt(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> gte(Expression<S> expr) {
        return Comparison.gte(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> gte(String text) {
        return Comparison.gte(this, text(text));
    }

    /**
     * @param number
     * @return
     */
    public Comparison<S> gte(Number number) {
        return Comparison.gte(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> lt(Expression<S> expr) {
        return Comparison.lt(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> lt(String text) {
        return Comparison.lt(this, text(text));
    }

    /**
     * @param number
     * @return
     */
    public Comparison<S> lt(Number number) {
        return Comparison.lt(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> lte(Expression<S> expr) {
        return Comparison.lte(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> lte(String text) {
        return Comparison.lte(this, text(text));
    }

    /**
     * @param number
     * @return
     */
    public Comparison<S> lte(Number number) {
        return Comparison.lte(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> neq(Expression<S> expr) {
        return Comparison.neq(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> neq(String text) {
        return Comparison.neq(this, text(text));
    }

    /**
     * @param number
     * @return
     */
    public Comparison<S> neq(Number number) {
        return Comparison.neq(this, number(number));
    }

    /**
     * @param expr
     * @return
     */
    public Comparison<S> like(Expression<S> expr) {
        return Comparison.like(this, expr);
    }

    /**
     * @param text
     * @return
     */
    public Comparison<S> like(String text) {
        return Comparison.like(this, text(text));
    }

    /**
     * @param lower
     * @param upper
     * @return
     */
    public Between<S> between(Expression<S> lower, Expression<S> upper) {
        return Between.between(this, lower, upper);
    }

    /**
     * @param lower
     * @param upper
     * @return
     */
    public Between<S> between(Number lower, Number upper) {
        return Between.between(this, number(lower), number(upper));
    }

    /**
     * @param values
     * @return
     */
    public In<S> in(Expression<S>... values) {
        return In.in(this, values);
    }

    /**
     * @param values
     * @return
     */
    public In<S> in(Number... values) {
        Literal<S>[] ns = new Literal[values.length];

        for (int i = 0, len = values.length; i < len; i++) {
            ns[i] = number(values[i]);
        }

        return In.in(this, ns);
    }

    /**
     * @param values
     * @return
     */
    public In<S> in(String... values) {
        Literal<S>[] ts = new Literal[values.length];

        for (int i = 0, len = values.length; i < len; i++) {
            ts[i] = text(values[i]);
        }

        return In.in(this, ts);
    }
}
