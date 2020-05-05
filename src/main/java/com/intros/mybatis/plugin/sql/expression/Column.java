package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.utils.StringUtils;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SPACE;

public class Column<S extends Sql<S>> extends Expression<S> {
    private static final Class<Column> THIS_CLASS = Column.class;

    static {
        registerFactory(THIS_CLASS, (initArgs -> {
            if (initArgs.length == 1) {
                return new Column((String) initArgs[0]);
            } else if (initArgs.length == 2) {
                return new Column((String) initArgs[0], (String) initArgs[1]);
            }

            return null;
        }));
    }

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
        return instance(THIS_CLASS, column);
    }

    public static Column column(String table, String column) {
        return instance(THIS_CLASS, table, column);
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
