package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.SQL;

import static com.intros.mybatis.plugin.sql.constants.Keywords.CLOSE_BRACKET;
import static com.intros.mybatis.plugin.sql.constants.Keywords.OPEN_BRACKET;

/**
 * @param <S>
 * @author teddy
 */
public class BracketCond<S extends SQL<S>> extends Condition<S> {
    private static final Class<BracketCond> THIS_CLASS = BracketCond.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new BracketCond((Condition) initArgs[0]));
    }

    private Condition<S> cond;

    protected BracketCond(Condition<S> cond) {
        this.cond = cond;
    }

    public static <S extends SQL<S>> BracketCond<S> priority(Condition<S> cond) {
        return instance(THIS_CLASS, cond);
    }

    @Override
    public S write(S sql) {
        return this.cond.write(sql.append(OPEN_BRACKET)).append(CLOSE_BRACKET);
    }
}
