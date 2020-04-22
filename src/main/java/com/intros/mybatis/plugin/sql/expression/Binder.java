package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.SQL;
import com.intros.mybatis.plugin.sql.constants.BindType;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * @param <S>
 */
public class Binder<S extends SQL<S>> extends Expression<S> {
    private static final Class<Binder> THIS_CLASS = Binder.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Binder((String) initArgs[0], (BindType) initArgs[1]));
    }

    private String param;

    private BindType bindType;

    protected Binder(String param, BindType bindType) {
        this.param = param;
        this.bindType = bindType;
    }

    public static <S extends SQL<S>> Binder<S> bind(String param) {
        return instance(THIS_CLASS, param, BIND);
    }

    public static <S extends SQL<S>> Binder<S> bind(String param, BindType bindType) {
        return instance(THIS_CLASS, param, bindType);
    }

    @Override
    public S write(S sql) {
        return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2).append(this.param).append(KW_PARAM_NAME_SUFFIX);
    }
}
