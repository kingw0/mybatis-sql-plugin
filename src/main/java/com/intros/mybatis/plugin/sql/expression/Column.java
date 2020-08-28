package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.BindType;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.utils.StringUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.SPACE;

public class Column<S extends Sql<S>> extends Expression<S> {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    private String table;

    private String column;

    private String alias;

    protected Column(String table, String column) {
        this.table = table;
        this.column = column;
    }

    protected Column(String column) {
        this(null, column);
    }

    public static Column column(String column) {
        return new Column(column);
    }

    public static Column column(String table, String column) {
        return new Column(table, column);
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
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass) {
        return columns(mappingClass, true);
    }


    /**
     * @param tableAlias
     * @param mappingClass
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(String tableAlias, Class<?> mappingClass) {
        return columns(tableAlias, mappingClass, true, null);
    }

    /**
     * @param tableAlias
     * @param mappingClass
     * @param filter
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(String tableAlias, Class<?> mappingClass, Predicate<ColumnInfo> filter) {
        return columns(tableAlias, mappingClass, true, filter);
    }

    /**
     * @param mappingClass
     * @param alias
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, boolean alias) {
        return columns(mappingClass, alias, null);
    }

    /**
     * @param mappingClass
     * @param alias
     * @param filter
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(Class<?> mappingClass, boolean alias, Predicate<ColumnInfo> filter) {
        return columns(null, mappingClass, alias, filter);
    }


    /**
     * @param tableAlias
     * @param mappingClass
     * @param alias
     * @param filter
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> List<Column<S>> columns(String tableAlias, Class<?> mappingClass, boolean alias, Predicate<ColumnInfo> filter) {
        return list(mappingClass, columnInfo -> {
            Column<S> column = StringUtils.isNotBlank(tableAlias) ? Column.column(tableAlias, columnInfo.column()) : Column.column(columnInfo.column());
            return alias ? column.as(columnInfo.prop()) : column;
        }, filter);
    }

    /**
     * @param mappingClass
     * @param filter
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(Class<?> mappingClass, Predicate<ColumnInfo> filter) {
        return bind(null, mappingClass, filter);
    }

    /**
     * @param mappingClass
     * @param filter
     * @param param
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(String param, Class<?> mappingClass, Predicate<ColumnInfo> filter) {
        return bind(param, mappingClass, filter, BIND);
    }

    /**
     * convert column infos to bind expression
     *
     * @param param
     * @param mappingClass
     * @param filter
     * @param bindType
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> Binder<S> bind(String param, Class<?> mappingClass, Predicate<ColumnInfo> filter, BindType bindType) {
        return Binder.bindMultiProps(param, bindType, list(mappingClass, columnInfo -> columnInfo.prop(), filter).toArray(new String[0]));
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

    public Column as(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public S write(S sql) {
        if (StringUtils.isNotBlank(table)) {
            sql.append(table).append(Keywords.DOT);
        }

        sql.append(this.column);

        if (StringUtils.isNotBlank(alias)) {
            sql.append(SPACE).append(alias);
        }

        return sql;
    }
}
