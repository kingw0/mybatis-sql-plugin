package com.intros.mybatis.plugin.converter;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Comparison;
import com.intros.mybatis.plugin.sql.condition.Condition;

import static com.intros.mybatis.plugin.sql.expression.Bind.bind;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

public class EqConditionConverter<S extends Sql<S>> implements ConditionConverter {
    @Override
    public Condition write(String column, String param) {
        return Comparison.<S>eq(column(column), bind(param));
    }
}
