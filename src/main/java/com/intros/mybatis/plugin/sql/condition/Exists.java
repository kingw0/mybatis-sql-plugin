package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * exists sql part
 *
 * @param <S>
 */
public class Exists<S extends Sql<S>> extends Condition<S> {
    private static final Class<Exists> THIS_CLASS = Exists.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Exists((Sql) initArgs[0]));
    }

    private Sql<S> sql;

    protected Exists(Sql<S> sql) {
        this.sql = sql;
    }

    public static <S extends Sql<S>> Exists<S> exists(Sql<S> sql) {
        return instance(THIS_CLASS, sql);
    }

    @Override
    public S write(S sql) {
        return sql.append(KW_EXISTS).append(OPEN_BRACKET).append(this.sql).append(CLOSE_BRACKET);
    }
}
