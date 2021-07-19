package com.intros.mybatis.plugin.sql.condition.generator;

import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.scripting.xmltags.OgnlCache;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SPACE;

/**
 * Generate {@link Condition} from {@link Criteria}
 *
 * @param <S>
 */
public interface ConditionGenerator<S extends Sql<S>> {
    Condition<S> build(Criteria criteria, String paramName, Object paramValue);

    default Condition<S> doBuild(Criteria criteria, String paramName, Object paramValue) {
        if (criteria.testNotNull() && paramValue == null) {
            return null;
        }

        if (criteria.testNotBlank() && paramValue instanceof String && StringUtils.isBlank((String) paramValue)) {
            return null;
        }

        if (StringUtils.isNotBlank(criteria.test())) {
            Object result = OgnlCache.getValue(criteria.test(), paramValue);

            if (result == null || (result instanceof Boolean && !(Boolean) result)) {
                return null;
            }
        }

        if (StringUtils.isNotBlank(criteria.expression())) {
            return new Condition<S>() {
                @Override
                public S write(S sql) {
                    return sql.append(SPACE).append(criteria.expression()).append(SPACE);
                }
            };
        }

        return build(criteria, paramName, paramValue);
    }
}
