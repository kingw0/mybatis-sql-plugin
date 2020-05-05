package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.SqlPart;

/**
 * Sql condition
 *
 * @author teddy
 */
public abstract class Condition<S extends Sql<S>> extends SqlPart<S> {
    /**
     * @param cond
     * @return
     */
    public Condition<S> and(Condition<S> cond) {
        return Binary.and(this, cond);
    }

    /**
     * @param cond
     * @return
     */
    public Condition<S> or(Condition<S> cond) {
        return Binary.or(this, cond);
    }

}
