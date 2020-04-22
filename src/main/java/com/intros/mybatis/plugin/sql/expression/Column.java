package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.SQL;
import com.intros.mybatis.plugin.utils.StringUtils;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SPACE;

public class Column<S extends SQL<S>> extends Expression<S> {
    private static final Class<Column> THIS_CLASS = Column.class;

    static {
        registerFactory(THIS_CLASS, (initArgs -> {
            if (initArgs.length == 1) {
                return new Column((String) initArgs[0]);
            }

            return null;
        }));
    }

    private String column;

    private String alias;

    protected Column(String column) {
        this.column = column;
    }

    public static Column column(String column) {
        return instance(THIS_CLASS, column);
    }

    public Column as(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public S write(S sql) {
        sql.append(this.column);

        if (StringUtils.isNotBlank(alias)) {
            sql.append(SPACE).append(alias);
        }

        return sql;
    }
}
