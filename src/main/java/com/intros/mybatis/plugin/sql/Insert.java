package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.expression.Expression;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * @author teddy
 */
public class Insert extends SQL<Insert> {
    public static final String INSERT = "INSERT INTO ";

    public static final String VALUES = " VALUES ";

    public Insert(String table) {
        append(INSERT).append(table);
    }

    public Insert columns(String... columns) {
        return Joiner.join(this, COMMA_WITH_SPACE, OPEN_BRACKET_WITH_SPACE, CLOSE_BRACKET, columns);
    }

    public Insert values(Expression<Insert>... values) {
        return Joiner.join(this.append(VALUES), COMMA_WITH_SPACE, OPEN_BRACKET, CLOSE_BRACKET, values);
    }
}
