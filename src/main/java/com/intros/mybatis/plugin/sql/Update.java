package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.expression.Expression;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;
import static com.intros.mybatis.plugin.sql.expression.Literal.number;
import static com.intros.mybatis.plugin.sql.expression.Literal.text;

public class Update extends Sql<Update> {

    private boolean first = true;

    public Update(String table) {
        this.append(KW_UPDATE).append(table).append(KW_SET);
    }

    public Update set(String column, Expression<Update> value) {
        return innerSet(column, value);
    }

    public Update set(String column, Number value) {
        return innerSet(column, number(value));
    }

    public Update set(String column, String value) {
        return innerSet(column, text(value));
    }

    public Update where(Condition<Update> condition) {
        return condition.write(this.append(KW_WHERE));
    }

    private Update innerSet(String column, Expression<Update> value) {
        if (!first) {
            this.append(COMMA_WITH_SPACE);
        } else {
            first = false;
        }

        return value.write(this.append(column).append(KW_EQU_WITH_SPACE));
    }
}
