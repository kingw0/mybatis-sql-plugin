package com.intros.mybatis.plugin.sql.condition.generator;

import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Condition;

import static com.intros.mybatis.plugin.sql.expression.Binder.bind;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

public class Eq<S extends Sql<S>> implements ConditionGenerator<S> {
    @Override
    public Condition<S> build(Criteria criteria, String paramName, Object paramValue) {
        return column(criteria.column()).eq(bind(paramName));
    }
}
