package com.intros.mybatis.plugin.converter;

import com.intros.mybatis.plugin.sql.condition.Condition;

/**
 *
 */
public interface ConditionConverter {
    Condition write(String column, String param);
}
