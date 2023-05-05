package cn.intros.mybatis.plugin.sql.expression;

import cn.intros.mybatis.plugin.sql.constants.Keywords;
import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.utils.StringUtils;

public class Column<S extends Sql<S>> extends Expression<S> {
    private String table;

    private String column;

    private Expression<S> columnExpr;

    private String alias;

    protected Column(String table, String column) {
        this.table = table;
        this.column = column;
    }

    protected Column(String table, Expression<S> columnExpr) {
        this.table = table;
        this.columnExpr = columnExpr;
    }

    protected Column(String column) {
        this(null, column);
    }

    protected Column(Expression<S> columnExpr) {
        this(null, columnExpr);
    }

    public static Column column(String column) {
        return new Column(column);
    }

    public static Column column(Expression columnExpr) {
        return new Column(columnExpr);
    }

    public static Column column(String table, String column) {
        return new Column(table, column);
    }

    public static Column column(String table, Expression column) {
        return new Column(table, column);
    }

    public Column<S> as(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public S write(S sql) {
        if (StringUtils.isNotBlank(table)) {
            sql.append(table).append(Keywords.DOT);
        }

        if (columnExpr != null) {
            columnExpr.write(sql);
        } else {
            sql.append(this.column);
        }

        if (StringUtils.isNotBlank(alias)) {
            sql.append(Keywords.KW_AS).append('`').append(alias).append('`');
        }

        return sql;
    }
}
