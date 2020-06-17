package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.expression.Expression;

import static com.intros.mybatis.plugin.sql.constants.Keywords.KW_ASC;
import static com.intros.mybatis.plugin.sql.constants.Keywords.KW_DESC;

public class Order<S extends Sql<S>> extends SqlPart<S> {
    private Expression<S> expression;

    private String order;

    protected Order(Expression<S> expression, String order) {
        this.expression = expression;
        this.order = order;
    }

    public static <S extends Sql<S>> Order asc(Expression<S> expression) {
        return new Order(expression, KW_ASC);
    }

    public static <S extends Sql<S>> Order desc(Expression<S> expression) {
        return new Order(expression, KW_DESC);
    }

    @Override
    public S write(S sql) {
        return expression.write(sql).append(order);
    }
}
