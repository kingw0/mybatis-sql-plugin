package com.intros.mybatis.plugin.sql;


import static com.intros.mybatis.plugin.sql.constants.Keywords.SEMICOLON_WITH_SPACE;

/**
 *
 */
public class MultiSql extends Sql<MultiSql> {
    private boolean multi = false;

    public MultiSql sql(Sql... sqls) {
        if (multi) {
            this.append(SEMICOLON_WITH_SPACE);
            multi = true;
        }

        append(sqls[0]);

        if (sqls.length > 1) {
            for (int i = 1, len = sqls.length; i < len; i++) {
                append(SEMICOLON_WITH_SPACE).append(sqls[i]);
            }
        }

        return this;
    }
}
