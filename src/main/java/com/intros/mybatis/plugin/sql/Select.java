package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.constants.Join;
import com.intros.mybatis.plugin.sql.constants.Order;
import com.intros.mybatis.plugin.sql.expression.Expression;
import javafx.util.Pair;

import java.io.IOException;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;

/**
 * Select sql
 *
 * @author teddy
 */
public class Select extends SQL<Select> {
    private boolean first = true;

    public Select() {
        append(KW_SELECT);
    }

    public Select columns(String... columns) {
        if (first) {
            first = false;
            return Joiner.join(this, COMMA_WITH_SPACE, columns);
        } else {
            return Joiner.join(this.append(COMMA_WITH_SPACE), COMMA_WITH_SPACE, columns);
        }
    }

    public Select columns(Expression<Select>... expressions) {
        if (first) {
            first = false;
            return Joiner.join(this, COMMA_WITH_SPACE, expressions);
        } else {
            return Joiner.join(this.append(COMMA_WITH_SPACE), COMMA_WITH_SPACE, expressions);
        }
    }

    public Select from(String table) {
        return this.append(KW_FROM).append(table);
    }

    public Select from(Table<Select> table) {
        return table.write(this.append(KW_FROM));
    }

    public Select join(String table, Join join) throws IOException {
        return this.append(join.join()).append(table);
    }

    public Select on(Condition<Select> condition) throws IOException {
        return condition.write(this.append(KW_ON));
    }

    public Select groupBy(String... column) {
        return Joiner.join(this.append(KW_GROUP_BY), COMMA_WITH_SPACE, column);
    }

    public Select having(Condition<Select> condition) {
        return condition.write(this.append(KW_HAVING));
    }

    public Select orderBy(Pair<String, Order>... orders) {
        append(KW_ORDER_BY);

        append(orders[0].getKey()).append(orders[0].getValue().order());

        for (int i = 1, len = orders.length; i < len; i++) {
            append(COMMA_WITH_SPACE).append(orders[i].getKey()).append(orders[i].getValue().order());
        }

        return this;
    }

    public Select where(Condition<Select> condition) {
        return condition.write(this.append(KW_WHERE));
    }

    public Select limit(int limit) {
        return append(KW_LIMIT).append(limit);
    }

    public Select offset(int offset) {
        return append(KW_OFFSET).append(offset);
    }
}
