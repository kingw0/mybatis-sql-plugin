package io.github.kingw0.mybatis.plugin.sql;

import io.github.kingw0.mybatis.plugin.sql.condition.Condition;
import io.github.kingw0.mybatis.plugin.sql.constants.Keywords;
import io.github.kingw0.mybatis.plugin.sql.expression.Expression;

import static io.github.kingw0.mybatis.plugin.sql.expression.Literal.number;
import static io.github.kingw0.mybatis.plugin.sql.expression.Literal.text;

public class Update extends Sql<Update> {

    private boolean first = true;

    public Update(String table) {
        this.append(Keywords.KW_UPDATE).append(table).append(Keywords.KW_SET);
    }

    public Update(Table<Update> table) {
        this.append(Keywords.KW_UPDATE).append(table).append(Keywords.KW_SET);
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
        return condition != null ? condition.write(this.append(Keywords.KW_WHERE)) : this;
    }

    private Update innerSet(String column, Expression<Update> value) {
        if (!first) {
            this.append(Keywords.COMMA_WITH_SPACE);
        } else {
            first = false;
        }

        return value.write(this.append(column).append(Keywords.KW_EQU_WITH_SPACE));
    }
}
