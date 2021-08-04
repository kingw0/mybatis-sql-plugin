package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.BindType;
import com.intros.mybatis.plugin.utils.StringUtils;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * @param <S>
 */
public class Binder<S extends Sql<S>> extends Expression<S> {
    protected String[] params;

    protected BindType bindType;

    protected Binder() {
    }

    public Binder(String... params) {
        this(BIND, params);
    }

    public Binder(BindType bindType, String... params) {
        if (params != null && params.length > 0) {
            this.params = params;
            this.bindType = bindType;
        } else {
            throw new IllegalArgumentException("Binder's params can not be null!");
        }
    }

    /**
     * @param params
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(String... params) {
        return new Binder<>(params);
    }


    /**
     * @param bindType
     * @param params
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(BindType bindType, String... params) {
        return new Binder<>(bindType, params);
    }

    /**
     * @param param
     * @param prop
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindProp(String param, String prop) {
        return bindProp(param, BIND, prop);
    }


    /**
     * @param param
     * @param bindType
     * @param prop
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindProp(String param, BindType bindType, String prop) {
        if (StringUtils.isBlank(param) && StringUtils.isBlank(prop)) {
            throw new IllegalArgumentException("param and prop can not be null at the same time!");
        }
        return StringUtils.isBlank(param)
                ? bind(prop) : (StringUtils.isBlank(prop) ? bind(param) : new PropBinder<>(param, bindType, prop));
    }

    /**
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindProps(String... props) {
        return bindProps(null, props);
    }

    /**
     * @param param
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindProps(String param, String... props) {
        return StringUtils.isBlank(param) ? bind(props) : bindProps(param, BIND, props);
    }

    /**
     * @param param
     * @param bindType
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindProps(String param, BindType bindType, String... props) {
        return new PropsBinder<>(param, bindType, props);
    }

    /**
     * @param param
     * @param index
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndex(String param, int index) {
        return bindIndex(param, BIND, index);
    }

    /**
     * @param param
     * @param bindType
     * @param index
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndex(String param, BindType bindType, int index) {
        return new IndexBinder<>(param, bindType, index);
    }

    /**
     * @param param
     * @param index
     * @param prop
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndexProp(String param, int index, String prop) {
        return bindIndexProp(param, BIND, index, prop);
    }

    /**
     * @param param
     * @param bindType
     * @param index
     * @param prop
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndexProp(String param, BindType bindType, int index, String prop) {
        return StringUtils.isBlank(prop) ? new IndexBinder<>(param, bindType, index)
                : new IndexPropBinder<>(param, bindType, index, prop);
    }

    /**
     * @param param
     * @param index
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndexProps(String param, int index, String... props) {
        return bindIndexProps(param, BIND, index, props);
    }

    /**
     * @param param
     * @param bindType
     * @param index
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndexProps(String param, BindType bindType, int index, String... props) {
        return new IndexPropsBinder<>(param, bindType, index, props);
    }

    /**
     * @param param
     * @param size
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndices(String param, int size) {
        return bindIndices(param, BIND, size);
    }

    /**
     * @param param
     * @param bindType
     * @param size
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndices(String param, BindType bindType, int size) {
        return new IndicesBinder<>(param, bindType, size);
    }

    @Override
    public S write(S sql) {
        int index = 0;

        String prefix = this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2;

        sql.append(prefix).append(this.params[index++]).append(KW_PARAM_NAME_SUFFIX);

        for (; index < this.params.length; index++) {
            sql.append(COMMA_WITH_SPACE).append(prefix).append(this.params[index]).append(KW_PARAM_NAME_SUFFIX);
        }

        return sql;
    }

    /**
     * Bind a param's property, eg. #{param.prop}
     *
     * @param <S>
     */
    public final static class PropBinder<S extends Sql<S>> extends Binder<S> {
        private String prop;

        public PropBinder(String param, BindType bindType, String prop) {
            super(bindType, param);
            this.prop = prop;
        }

        @Override
        public S write(S sql) {
            return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(this.params[0]).append(DOT).append(this.prop)
                    .append(KW_PARAM_NAME_SUFFIX);
        }
    }

    /**
     * Bind multi props of param,eg. #{param.prop1}, #{param.prop2}
     *
     * @param <S>
     */
    public final static class PropsBinder<S extends Sql<S>> extends Binder<S> {
        private String[] props;

        public PropsBinder(String param, BindType bindType, String[] props) {
            super(bindType, param);
            this.props = props;
        }

        @Override
        public S write(S sql) {
            final String prefix =
                    (this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2) + this.params[0] + DOT;

            int index = 0;

            sql.append(prefix).append(props[index++]).append(KW_PARAM_NAME_SUFFIX);

            for (; index < props.length; index++) {
                sql.append(COMMA_WITH_SPACE).append(prefix).append(props[index]).append(KW_PARAM_NAME_SUFFIX);
            }

            return sql;
        }
    }

    /**
     * Bind a param's element at position index point to,eg. #{param[0]}
     *
     * @param <S>
     */
    public final static class IndexBinder<S extends Sql<S>> extends Binder<S> {
        private int index;

        public IndexBinder(String param, BindType bindType, int index) {
            super(bindType, param);
            this.index = index;
        }

        @Override
        public S write(S sql) {
            return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(this.params[0])
                    .append(OPEN_SQUARE_BRACKET).append(this.index).append(CLOSE_SQUARE_BRACKET)
                    .append(KW_PARAM_NAME_SUFFIX);
        }
    }

    public final static class IndicesBinder<S extends Sql<S>> extends Binder<S> {
        private int size;

        public IndicesBinder(String param, BindType bindType, int size) {
            super(bindType, param);
            this.size = size;
        }

        @Override
        public S write(S sql) {
            String prefix =
                    (this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2) + this.params[0] + OPEN_SQUARE_BRACKET;

            int index = 0;

            sql.append(prefix).append(index++).append(CLOSE_SQUARE_BRACKET).append(KW_PARAM_NAME_SUFFIX);

            for (; index < this.size; index++) {
                sql.append(COMMA_WITH_SPACE).append(prefix).append(index).append(CLOSE_SQUARE_BRACKET).append(KW_PARAM_NAME_SUFFIX);
            }

            return sql;
        }
    }

    /**
     * Bind a prop of the param's element. eg. #{param[index].prop}
     *
     * @param <S>
     */
    public final static class IndexPropBinder<S extends Sql<S>> extends Binder<S> {
        private int index;

        private String prop;

        public IndexPropBinder(String param, BindType bindType, int index, String prop) {
            super(bindType, param);
            this.index = index;
            this.prop = prop;
        }

        @Override
        public S write(S sql) {
            return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(this.params[0]).append(OPEN_SQUARE_BRACKET).append(this.index).append(CLOSE_SQUARE_BRACKET)
                    .append(DOT).append(this.prop)
                    .append(KW_PARAM_NAME_SUFFIX);
        }
    }

    /**
     * @param <S>
     */
    public final static class IndexPropsBinder<S extends Sql<S>> extends Binder<S> {
        private int index;

        private String[] props;

        public IndexPropsBinder(String param, BindType bindType, int index, String[] props) {
            super(bindType, param);
            this.index = index;
            this.props = props;
        }

        @Override
        public S write(S sql) {
            final StringBuilder prefix = new StringBuilder(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(this.params[0]).append(OPEN_SQUARE_BRACKET).append(this.index).append(CLOSE_SQUARE_BRACKET).append(DOT);

            int index = 0;

            sql.append(prefix).append(props[index++]).append(KW_PARAM_NAME_SUFFIX);

            for (int len = props.length; index < len; index++) {
                sql.append(COMMA_WITH_SPACE).append(prefix).append(props[index]).append(KW_PARAM_NAME_SUFFIX);
            }

            return sql;
        }
    }
}
