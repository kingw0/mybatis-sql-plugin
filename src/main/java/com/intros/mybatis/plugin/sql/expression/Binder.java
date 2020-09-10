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
    protected String param;

    protected BindType bindType;

    protected Binder() {
    }

    public Binder(String param) {
        this(param, BIND);
    }

    public Binder(String param, BindType bindType) {
        this.param = param;
        this.bindType = bindType;
    }

    /**
     * @param param
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(String param) {
        return new Binder<>(param);
    }

    /**
     * @param param
     * @param bindType
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(String param, BindType bindType) {
        return new Binder<>(param, bindType);
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
        return new PropBinder<>(param, bindType, prop);
    }

    /**
     * @param param
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindMultiProps(String param, String... props) {
        return bindMultiProps(param, BIND, props);
    }

    /**
     * @param param
     * @param bindType
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindMultiProps(String param, BindType bindType, String... props) {
        return new MultiPropsBinder<>(param, bindType, props);
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
        return new IndexPropBinder<>(param, bindType, index, prop);
    }

    /**
     * @param param
     * @param index
     * @param props
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindIndexProps(String param, int index, String... props) {
        return new IndexMultiPropsBinder<>(param, BIND, index, props);
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
        return new IndexMultiPropsBinder<>(param, bindType, index, props);
    }

    /**
     * @param param
     * @param size
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindMultiIndex(String param, int size) {
        return bindMultiIndex(param, BIND, size);
    }

    /**
     * @param param
     * @param bindType
     * @param size
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindMultiIndex(String param, BindType bindType, int size) {
        return new MultiIndexBinder<>(param, bindType, size);
    }

    /**
     * @param param
     * @param size
     * @param prop
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindMultiIndexProp(String param, int size, String prop) {
        return bindMultiIndexProp(param, BIND, size, prop);
    }

    /**
     * @param param
     * @param bindType
     * @param size
     * @param prop
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bindMultiIndexProp(String param, BindType bindType, int size, String prop) {
        return new MultiIndexPropBinder<>(param, bindType, size, prop);
    }


    @Override
    public S write(S sql) {
        return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                .append(this.param)
                .append(KW_PARAM_NAME_SUFFIX);
    }

    /**
     * Bind a param's property, eg. #{param.prop}
     *
     * @param <S>
     */
    public final static class PropBinder<S extends Sql<S>> extends Binder<S> {
        private String prop;

        public PropBinder(String param, BindType bindType, String prop) {
            super(param, bindType);
            this.prop = prop;
        }

        @Override
        public S write(S sql) {
            sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2);

            if (StringUtils.isNotBlank(this.param)) {
                sql.append(this.param).append(DOT);
            }

            sql.append(this.prop).append(KW_PARAM_NAME_SUFFIX);

            return sql;
        }
    }

    /**
     * Bind multi props of param,eg. #{param.prop1}, #{param.prop2}
     *
     * @param <S>
     */
    public final static class MultiPropsBinder<S extends Sql<S>> extends Binder<S> {
        private String[] props;

        public MultiPropsBinder(String param, BindType bindType, String[] props) {
            super(param, bindType);
            this.props = props;
        }

        @Override
        public S write(S sql) {
            final String prefix = (this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2) + (StringUtils.isNotBlank(this.param) ? (param + DOT) : "");

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
            super(param, bindType);
            this.index = index;
        }

        @Override
        public S write(S sql) {
            return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(this.param).append(OPEN_SQUARE_BRACKET).append(this.index).append(CLOSE_SQUARE_BRACKET)
                    .append(KW_PARAM_NAME_SUFFIX);
        }
    }

    public final static class MultiIndexBinder<S extends Sql<S>> extends Binder<S> {
        private int size;

        public MultiIndexBinder(String param, BindType bindType, int size) {
            super(param, bindType);
            this.size = size;
        }

        @Override
        public S write(S sql) {
            String prefix = (this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2) + this.param + OPEN_SQUARE_BRACKET;

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
            super(param, bindType);
            this.index = index;
            this.prop = prop;
        }

        @Override
        public S write(S sql) {
            return sql.append(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(this.param).append(OPEN_SQUARE_BRACKET).append(this.index).append(CLOSE_SQUARE_BRACKET)
                    .append(DOT).append(this.prop)
                    .append(KW_PARAM_NAME_SUFFIX);
        }
    }

    public final static class MultiIndexPropBinder<S extends Sql<S>> extends Binder<S> {
        private int size;

        private String prop;

        public MultiIndexPropBinder(String param, BindType bindType, int size, String prop) {
            super(param, bindType);
            this.size = size;
            this.prop = prop;
        }

        @Override
        public S write(S sql) {
            String prefix = (this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2) + this.param + OPEN_SQUARE_BRACKET;

            int index = 0;

            sql.append(prefix).append(index++).append(CLOSE_SQUARE_BRACKET).append(DOT).append(this.prop).append(KW_PARAM_NAME_SUFFIX);

            for (; index < this.size; index++) {
                sql.append(COMMA_WITH_SPACE).append(prefix).append(index).append(CLOSE_SQUARE_BRACKET).append(DOT).append(this.prop).append(KW_PARAM_NAME_SUFFIX);
            }

            return sql;
        }
    }

    /**
     * @param <S>
     */
    public final static class IndexMultiPropsBinder<S extends Sql<S>> extends Binder<S> {
        private int index;

        private String[] props;

        public IndexMultiPropsBinder(String param, BindType bindType, int index, String[] props) {
            super(param, bindType);
            this.index = index;
            this.props = props;
        }

        @Override
        public S write(S sql) {
            final StringBuilder prefixBuffer = new StringBuilder(this.bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2)
                    .append(param).append(OPEN_SQUARE_BRACKET).append(this.index).append(CLOSE_SQUARE_BRACKET).append(DOT);

            int index = 0;

            sql.append(prefixBuffer).append(props[index++]).append(KW_PARAM_NAME_SUFFIX);

            for (int len = props.length; index < len; index++) {
                sql.append(COMMA_WITH_SPACE).append(prefixBuffer).append(props[index]).append(KW_PARAM_NAME_SUFFIX);
            }

            return sql;
        }
    }
}
