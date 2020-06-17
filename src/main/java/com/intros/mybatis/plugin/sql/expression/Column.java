package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.utils.StringUtils;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SPACE;

public class Column<S extends Sql<S>> extends Expression<S> {
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
