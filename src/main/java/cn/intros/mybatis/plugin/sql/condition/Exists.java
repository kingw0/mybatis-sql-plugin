package cn.intros.mybatis.plugin.sql.condition;

import cn.intros.mybatis.plugin.sql.constants.Keywords;
import cn.intros.mybatis.plugin.sql.Sql;

/**
 * exists sql part
 *
 * @param <S>
 */
public class Exists<S extends Sql<S>> extends Condition<S> {
    private Sql<S> sql;

    protected Exists(Sql<S> sql) {
        this.sql = sql;
    }

    public static <S extends Sql<S>> Exists<S> exists(Sql<S> sql) {
        return new Exists<>(sql);
    }

    @Override
    public S write(S sql) {
        return sql.append(Keywords.KW_EXISTS).append(Keywords.OPEN_BRACKET).append(this.sql).append(Keywords.CLOSE_BRACKET);
    }
}
