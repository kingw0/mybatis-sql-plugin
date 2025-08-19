package io.github.kingw0.mybatis.plugin.sql;

import io.github.kingw0.mybatis.plugin.sql.constants.Keywords;
import io.github.kingw0.mybatis.plugin.sql.expression.Expression;

import java.util.List;

import static io.github.kingw0.mybatis.plugin.sql.constants.Keywords.*;
import static io.github.kingw0.mybatis.plugin.sql.expression.Literal.number;
import static io.github.kingw0.mybatis.plugin.sql.expression.Literal.text;

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

    public Insert duplicate() {
        return this.append(" ON DUPLICATE KEY UPDATE ");
    }

    public Insert set(String column, Number value) {
        return set(column, number(value));
    }

    public Insert set(String column, String value) {
        return set(column, text(value));
    }

    public Insert set(String column, Expression<Insert> value) {
        return value.write(this.append(column).append(Keywords.KW_EQU_WITH_SPACE));
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
