package io.github.kingw0.mybatis.plugin.sql.condition;

import io.github.kingw0.mybatis.plugin.sql.Select;
import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.constants.Keywords;

/**
 * exists sql part
 *
 * @param <S>
 */
public class Exists<S extends Sql<S>> extends Condition<S> {
    private Select sql;

    protected Exists(Select sql) {
        this.sql = sql;
    }

    public static <S extends Sql<S>> Exists<S> exists(Select sql) {
        return new Exists<>(sql);
    }

    @Override
    public S write(S sql) {
        return sql.append(Keywords.KW_EXISTS).append(Keywords.OPEN_BRACKET).append(this.sql)
            .append(Keywords.CLOSE_BRACKET);
    }
}
