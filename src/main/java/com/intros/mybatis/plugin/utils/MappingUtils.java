package com.intros.mybatis.plugin.utils;

import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.expression.Binder;
import com.intros.mybatis.plugin.sql.expression.Column;
import com.intros.mybatis.plugin.sql.expression.Expression;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingUtils {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    /**
     * @param mappingClass
     * @param predicate
     * @return
     */
    public static List<String> columns(Class<?> mappingClass, Predicate<ColumnInfo> predicate) {
        return columnInfoMap(mappingClass, ColumnInfo::column, predicate);
    }

    /**
     * @param mappingClass
     * @param table
     * @param predicate
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, final String table, Predicate<ColumnInfo> predicate) {
        return columnInfoMap(mappingClass, columnInfo -> Column.column(table, columnInfo.column()).as(columnInfo.alias()), predicate);
    }

    /**
     * @param mappingClass
     * @param predicate
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<? extends Expression<S>> values(Class<?> mappingClass, Predicate<ColumnInfo> predicate) {
        return columnInfoMap(mappingClass, columnInfo -> Binder.bind(columnInfo.alias()), predicate);
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
     * @param predicate
     * @param consumer
     */
    public static void consume(Class<?> mappingClass, Predicate<ColumnInfo> predicate, Consumer<ColumnInfo> consumer) {
        Stream<ColumnInfo> stream = registry.mappingInfo(mappingClass).columnInfos().stream();

        if (predicate != null) {
            stream = stream.filter(predicate);
        }

        stream.forEach(consumer);
    }
}
