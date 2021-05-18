package com.intros.mybatis.plugin.sql.condition.generator;

import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Condition;

/**
 * Generate {@link Condition} from {@link Criteria}
 *
 * @param <S>
 */
public interface ConditionGenerator<S extends Sql<S>> {
    Condition<S> build(Criteria criteria, String paramName, Object paramValue);

    default Condition<S> doBuild(Criteria criteria, String paramName, Object paramValue) {
        if (!criteria.nullable() && paramValue == null) {
            return null;
        }

        return build(criteria, paramName, paramValue);
    }
}
