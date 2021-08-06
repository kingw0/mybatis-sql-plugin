package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.sql.expression.Column;
import com.intros.mybatis.plugin.utils.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <S>
 */
public class Table<S extends Sql<S>> extends SqlPart<S> {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    private Class<?> mappingClass;

    private String table;

    private String alias;

    private List<Column<S>> columns;

    protected Table(String table) {
        this.table = table;
    }

    public Table(Class<?> mappingClass) {
        MappingInfo mappingInfo = registry.mappingInfo(mappingClass);

        this.table = mappingInfo.table();

        this.mappingClass = mappingClass;
    }

    public static Table table(String table) {
        return new Table(table);
    }

    public static Table table(Class<?> mappingClass) {
        return new Table(mappingClass);
    }

    public Table as(String alias) {
        this.alias = alias;
        return this;
    }

    public Column<S> column(String column) {
        return Column.column(this.alias, column);
    }

    public List<Column<S>> columns(String... columns) {
        return Arrays.stream(columns).map(this::column).collect(Collectors.toList());
    }

    public List<Column<S>> columns() {
        if (this.mappingClass == null) {
            throw new IllegalStateException("Mapping class is null!");
        }

        this.columns = new LinkedList<>();

        for (ColumnInfo columnInfo : registry.mappingInfo(this.mappingClass).columnInfos()) {
            columns.add(Column.column(this.alias, columnInfo.column()).as(columnInfo.prop()));
        }
        return this.columns;
    }

    @Override
    public S write(S sql) {
        if (!StringUtils.isBlank(this.alias)) {
            return sql.append(this.table).append(Keywords.SPACE).append(this.alias);
        } else {
            return sql.append(this.table);
        }
    }
}
