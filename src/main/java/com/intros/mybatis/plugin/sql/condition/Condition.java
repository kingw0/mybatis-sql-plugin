package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.SqlPart;

import static com.intros.mybatis.plugin.sql.constants.Keywords.CLOSE_BRACKET;
import static com.intros.mybatis.plugin.sql.constants.Keywords.OPEN_BRACKET;

/**
 * Sql condition
 *
 * @author teddy
 */
public abstract class Condition<S extends Sql<S>> extends SqlPart<S> {
    /**
     * Condition surround by bracket
     *
     * @param Condition
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Condition<S> bracket(final Condition<S> Condition) {
        return new Condition<S>() {
            @Override
            public S write(S sql) {
                return Condition.write(sql.append(OPEN_BRACKET)).append(CLOSE_BRACKET);
            }
        };
    }

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
