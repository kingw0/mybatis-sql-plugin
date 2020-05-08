package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Joiner;
import com.intros.mybatis.plugin.sql.Sql;

import java.util.Arrays;
import java.util.List;

import static com.intros.mybatis.plugin.sql.constants.Keywords.COMMA_WITH_SPACE;

/**
 * @param <S>
 * @author teddy
 */
public class ExpressionList<S extends Sql<S>> extends Expression<S> {
    
    private List<? extends Expression<S>> expressions;

    protected ExpressionList(List<? extends Expression<S>> expressions) {
        this.expressions = expressions;
    }

    public static <S extends Sql<S>> ExpressionList<S> list(List<? extends Expression<S>> expressions) {
        return new ExpressionList<S>(expressions);
    }

    public static <S extends Sql<S>> ExpressionList<S> list(Expression<S>... expressions) {
        return new ExpressionList<S>(Arrays.asList(expressions));
    }

    @Override
    public S write(S sql) {
        return Joiner.join(sql, COMMA_WITH_SPACE, this.expressions);
    }
}
