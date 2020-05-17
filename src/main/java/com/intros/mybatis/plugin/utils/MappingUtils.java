package com.intros.mybatis.plugin.utils;

import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.BindType;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.sql.expression.Bind;
import com.intros.mybatis.plugin.sql.expression.Column;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

public class MappingUtils {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    /**
     * @param mappingClass
     * @param predicate
     * @return
     */
    public static List<String> columnNames(Class<?> mappingClass, Predicate<ColumnInfo> predicate) {
        return columnInfoMap(mappingClass, ColumnInfo::column, predicate);
    }

    /**
     * @param mappingClass
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass) {
        return columns(mappingClass, null, null);
    }

    /**
     * @param mappingClass
     * @param table
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, final String table) {
        return columns(mappingClass, table, null);
    }

    /**
     * @param mappingClass
     * @param predicate
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, Predicate<ColumnInfo> predicate) {
        return columns(mappingClass, null, predicate);
    }

    /**
     * @param mappingClass
     * @param table
     * @param predicate
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, final String table, Predicate<ColumnInfo> predicate) {
        return columnInfoMap(mappingClass, columnInfo -> Column.column(table, columnInfo.column()).as(columnInfo.prop()), predicate);
    }

    /**
     * @param mappingClass
     * @param table
     * @param filter
     * @param props
     * @return
     */
    public static String columns(Class<?> mappingClass, final String table, Predicate<ColumnInfo> filter, boolean props) {
        boolean hasTable = StringUtils.isNotBlank(table);

        String prefix = hasTable ? table + DOT : "";

        StringBuilder buffer = new StringBuilder();

        consume(mappingClass, filter, new Consumer<ColumnInfo>() {
            private boolean first = true;

            @Override
            public void accept(ColumnInfo columnInfo) {
                if (first) {
                    buffer.append(COMMA_WITH_SPACE);
                    first = false;
                }

                if (hasTable) {
                    buffer.append(prefix).append(columnInfo.column());
                } else {
                    buffer.append(columnInfo.column());
                }

                if (props) {
                    buffer.append(SPACE).append(columnInfo.prop());
                }
            }
        });

        return buffer.toString();
    }

    /**
     * @param mappingClass
     * @param filter
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(Class<?> mappingClass, Predicate<ColumnInfo> filter) {
        return bind(mappingClass, filter, null);
    }

    /**
     * @param mappingClass
     * @param filter
     * @param param
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(Class<?> mappingClass, Predicate<ColumnInfo> filter, String param) {
        return bind(mappingClass, filter, param, BIND);
    }

    /**
     * convert column infos to bind expression
     *
     * @param mappingClass
     * @param filter
     * @param param
     * @param bindType
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Bind<S> bind(Class<?> mappingClass, Predicate<ColumnInfo> filter, String param, BindType bindType) {
        return Bind.bind(param, list(mappingClass, columnInfo -> columnInfo.prop(), filter), bindType);
    }


    /**
     * convert column infos to bind expression string
     *
     * @param mappingClass
     * @param filter
     * @param param
     * @return
     */
    public static String bindExpr(Class<?> mappingClass, Predicate<ColumnInfo> filter, String param, BindType bindType) {
        String prefix = StringUtils.isNotBlank(param) ? (bindType == BIND ? KW_PARAM_NAME_PREFIX : KW_PARAM_NAME_PREFIX2 + param + Keywords.DOT) : KW_PARAM_NAME_PREFIX;

        StringBuilder buffer = new StringBuilder();

        Iterator<String> iter = list(mappingClass, columnInfo -> prefix + columnInfo.prop() + KW_PARAM_NAME_SUFFIX, filter).iterator();

        if (iter.hasNext()) {
            buffer.append(iter.next());

            while (iter.hasNext()) {
                buffer.append(COMMA_WITH_SPACE).append(iter.next());
            }
        }

        return buffer.toString();
    }

    /**
     * Mapping column infos of mapping class to List
     *
     * @param mappingClass
     * @param mapper
     * @param filter
     * @param <R>
     * @return
     */
    public static <R> List<R> list(Class<?> mappingClass, Function<ColumnInfo, R> mapper, Predicate<ColumnInfo> filter) {
        return mapping(mappingClass, columnInfos -> columnInfos.stream().filter(filter).map(mapper).collect(Collectors.toList()));
    }

    /**
     * Mapping column infos of mapping class to R
     *
     * @param mappingClass
     * @param mapper
     * @param <R>
     * @return
     */
    public static <R> R mapping(Class<?> mappingClass, Function<List<ColumnInfo>, R> mapper) {
        return mapper.apply(registry.mappingInfo(mappingClass).columnInfos());
    }

    /**
     * @param mappingClass
     * @param mapper
     * @param predicate
     * @param <R>
     * @return
     */
    public static <R> List<R> columnInfoMap(Class<?> mappingClass, Function<ColumnInfo, R> mapper, Predicate<ColumnInfo> predicate) {
        Stream<ColumnInfo> stream = registry.mappingInfo(mappingClass).columnInfos().stream();

        if (predicate != null) {
            stream = stream.filter(predicate);
        }

        return stream.map(mapper).collect(Collectors.toList());
    }

    /**
     * @param mappingClass
     * @param filter
     * @param consumer
     */
    public static void consume(Class<?> mappingClass, Predicate<ColumnInfo> filter, Consumer<ColumnInfo> consumer) {
        Stream<ColumnInfo> stream = registry.mappingInfo(mappingClass).columnInfos().stream();

        if (filter != null) {
            stream = stream.filter(filter);
        }

        stream.forEach(consumer);
    }
}
