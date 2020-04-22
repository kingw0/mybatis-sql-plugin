package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.SQL;
import com.intros.mybatis.plugin.sql.SQLPart;

/**
 * Sql condition
 *
 * @author teddy
 */
public abstract class Condition<S extends SQL<S>> extends SQLPart<S> {
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
