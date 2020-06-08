package com.intros.mybatis.plugin.utils;

import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.Table;
import com.intros.mybatis.plugin.sql.constants.BindType;
import com.intros.mybatis.plugin.sql.expression.Bind;
import com.intros.mybatis.plugin.sql.expression.Column;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;

public class MappingUtils {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    public static <S extends Sql<S>> Table<S> table(Class<?> mappingClass) {
        return table(mappingClass, null);
    }

    public static <S extends Sql<S>> Table<S> table(Class<?> mappingClass, String alias) {
        return Table.table(registry.mappingInfo(mappingClass).table()).as(alias);
    }

    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass) {
        return columns(mappingClass, true);
    }

    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, String table) {
        return columns(mappingClass, table, true, null);
    }

    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, boolean alias) {
        return columns(mappingClass, alias, null);
    }

    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, boolean alias, Predicate<ColumnInfo> filter) {
        return columns(mappingClass, null, alias, filter);
    }

    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, String table, boolean alias, Predicate<ColumnInfo> filter) {
        return list(mappingClass, columnInfo -> {
            Column<S> column = StringUtils.isNotBlank(table) ? Column.column(table, columnInfo.column()) : Column.column(columnInfo.column());
            return alias ? column.as(columnInfo.prop()) : column;
        }, filter);
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
     * Mapping column infos of mapping class to List
     *
     * @param mappingClass
     * @param mapper
     * @param filter
     * @param <R>
     * @return
     */
    public static <R> List<R> list(Class<?> mappingClass, Function<ColumnInfo, R> mapper, Predicate<ColumnInfo> filter) {
        if (filter != null) {
            return mapping(mappingClass, columnInfos -> columnInfos.stream().filter(filter).map(mapper).collect(Collectors.toList()));
        } else {
            return mapping(mappingClass, columnInfos -> columnInfos.stream().map(mapper).collect(Collectors.toList()));
        }
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
