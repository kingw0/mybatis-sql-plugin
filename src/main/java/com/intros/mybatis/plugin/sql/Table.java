package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.sql.expression.Column;
import com.intros.mybatis.plugin.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <S>
 */
public class Table<S extends Sql<S>> extends SqlPart<S> {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    private String table;

    private String alias;

    protected Table(String table) {
        this.table = table;
    }

    public static Table table(String table) {
        return new Table(table);
    }

    public static Table table(Class<?> mappingClass) {
        return table(registry.mappingInfo(mappingClass).table());
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

    public List<Column<S>> columns(Class<?> mappingClass) {
        return Column.columns(this.alias, mappingClass);
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
