package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.expression.Expression;

import java.util.List;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * @author teddy
 */
public class Insert extends Sql<Insert> {
    public static final String INSERT = "INSERT INTO ";

    public static final String VALUES = " VALUES ";

    private int valuesCount = 0;

    public Insert(String table) {
        append(INSERT).append(table);
    }

    public Insert(Table table) {
        append(INSERT).append(table);
    }

    public Insert columns(List<String> columns) {
        return Joiner.joins(this, COMMA_WITH_SPACE, OPEN_BRACKET_WITH_SPACE, CLOSE_BRACKET, columns);
    }

    public Insert columns(String... columns) {
        return Joiner.join(this, COMMA_WITH_SPACE, OPEN_BRACKET_WITH_SPACE, CLOSE_BRACKET, columns);
    }

    public Insert values(List<? extends Expression<Insert>> values) {
        if (valuesCount == 0) {
            this.append(VALUES);
        } else {
            this.append(COMMA_WITH_SPACE);
        }

        valuesCount++;

        return Joiner.join(this, COMMA_WITH_SPACE, OPEN_BRACKET, CLOSE_BRACKET, values);
    }

    public Insert values(Expression<Insert>... values) {
        if (valuesCount == 0) {
            this.append(VALUES);
        } else {
            this.append(COMMA_WITH_SPACE);
        }

        valuesCount++;

        return Joiner.join(this, COMMA_WITH_SPACE, OPEN_BRACKET, CLOSE_BRACKET, values);
    }
}
