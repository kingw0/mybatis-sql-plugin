package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.BindType;

import java.util.Iterator;
import java.util.List;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * @param <S>
 */
public class Bind<S extends Sql<S>> extends Expression<S> {
    private String param;

    private int size = -1;

    private List<String> props;

    private BindType bindType;

    /**
     * bind or paste param,such as #{param}
     *
     * @param param
     * @param bindType
     */
    protected Bind(String param, BindType bindType) {
        this.param = param;
        this.bindType = bindType;
    }

    /**
     * bind or paste param's props,such as #{param.prop}
     *
     * @param param
     * @param props
     * @param bindType
     */
    protected Bind(String param, List<String> props, BindType bindType) {
        this.param = param;
        this.props = props;
        this.bindType = bindType;
    }

    /**
     * bind or paste param as collection,such as #{param[0]}
     *
     * @param param
     * @param size
     * @param bindType
     */
    protected Bind(String param, int size, BindType bindType) {
        this.param = param;
        this.size = size;
        this.bindType = bindType;
    }

    /**
     * bind param expression, such as #{param}
     *
     * @param param
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(String param) {
        return new Bind<>(param, BIND);
    }

    /**
     * bind or paste param expression
     *
     * @param param
     * @param bindType
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(String param, BindType bindType) {
        return new Bind<>(param, bindType);
    }

    /**
     * bind param's props expression,such as #{param.prop}
     *
     * @param param
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(String param, List<String> props) {
        return new Bind<>(param, props, BIND);
    }

    /**
     * bind or paste param's props expression
     *
     * @param param
     * @param props
     * @param bindType
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(String param, List<String> props, BindType bindType) {
        return new Bind<>(param, props, bindType);
    }

    /**
     * bind param as collection, such as #{param[0]}
     *
     * @param param
     * @param size
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(String param, int size) {
        return new Bind<>(param, size, BIND);
    }

    /**
     * bind param as collection
     *
     * @param param
     * @param size
     * @param bindType
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(String param, int size, BindType bindType) {
        return new Bind<>(param, size, bindType);
    }

    @Override
    public S write(S sql) {
        boolean bind = this.bindType == BIND;

        String prefix = bind ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2;

        if (size > 0) {
            // #{param[0]}, #{param[1]}...
            String p = prefix + param + OPEN_SQUARE_BRACKET;

            sql.append(p).append(0).append(CLOSE_SQUARE_BRACKET).append(KW_PARAM_NAME_SUFFIX);

            for (int i = 1; i < size; i++) {
                sql.append(COMMA_WITH_SPACE).append(p).append(i).append(CLOSE_SQUARE_BRACKET).append(KW_PARAM_NAME_SUFFIX);
            }

            return sql;
        } else if (props != null && props.size() > 0) {
            // #{param.prop}, #{param.prop}...
            String p = prefix + param + DOT;

            Iterator<String> iter = props.iterator();

            if (iter.hasNext()) {
                sql.append(p).append(iter.next()).append(KW_PARAM_NAME_SUFFIX);

                while (iter.hasNext()) {
                    sql.append(COMMA_WITH_SPACE).append(p).append(iter.next()).append(KW_PARAM_NAME_SUFFIX);
                }
            }

            return sql;
        }

        return sql.append(prefix).append(this.param).append(KW_PARAM_NAME_SUFFIX);
    }
}
