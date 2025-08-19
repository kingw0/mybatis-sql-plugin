package io.github.kingw0.mybatis.plugin.sql;

import io.github.kingw0.mybatis.plugin.mapping.MappingInfo;
import io.github.kingw0.mybatis.plugin.mapping.MappingInfoRegistry;
import io.github.kingw0.mybatis.plugin.sql.expression.Column;
import io.github.kingw0.mybatis.plugin.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.kingw0.mybatis.plugin.sql.constants.Keywords.*;

/**
 * @param <S>
 */
public class Table<S extends Sql<S>> extends SqlPart<S> {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    private Class<?> mappingClass;

    private String table;

    private Select select;

    private String alias;

    protected Table(String table) {
        this.table = table;
    }

    protected Table(Select select) {
        this.select = select;
    }

    public Table(Class<?> mappingClass) {
        MappingInfo mappingInfo = registry.mappingInfo(mappingClass);

        this.table = mappingInfo.table();

        this.mappingClass = mappingClass;
    }

    public static Table table(String table) {
        return new Table(table);
    }

    public static Table table(Select select) {
        return new Table(select);
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
        if (this.mappingClass == null) {
            throw new IllegalStateException("Mapping class is null!");
        }

        Set<String> columnSet = Arrays.stream(columns).collect(Collectors.toSet());

        return registry.mappingInfo(this.mappingClass).columnInfos().stream()
            .filter(columnInfo -> columnSet.contains(columnInfo.column()))
            .map(columnInfo -> Column.<S>column(this.alias, columnInfo.column()).as(columnInfo.prop()))
            .collect(Collectors.toList());
    }

    public List<Column<S>> columns() {
        if (this.mappingClass == null) {
            throw new IllegalStateException("Mapping class is null!");
        }

        return registry.mappingInfo(this.mappingClass).columnInfos().stream()
            .map(columnInfo -> Column.<S>column(this.alias, columnInfo.column()).as(columnInfo.prop()))
            .collect(Collectors.toList());
    }

    @Override
    public S write(S sql) {
        if (select == null) {
            sql.append(this.table);
        } else {
            sql.append(OPEN_BRACKET).append(select).append(CLOSE_BRACKET);
        }

        if (!StringUtils.isBlank(this.alias)) {
            sql.append(KW_AS).append(this.alias);
        }

        return sql;
    }
}
