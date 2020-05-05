package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.utils.StringUtils;

/**
 * @param <S>
 */
public class Table<S extends Sql<S>> extends SqlPart<S> {
    private static final Class<Table> THIS_CLASS = Table.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Table((String) initArgs[0]));
    }

    private String table;

    private String alias;

    protected Table(String table) {
        this.table = table;
    }

    public static Table table(String table) {
        return instance(THIS_CLASS, table);
    }

    public Table as(String alias) {
        this.alias = alias;
        return this;
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
