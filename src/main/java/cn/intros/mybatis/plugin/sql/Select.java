package cn.intros.mybatis.plugin.sql;

import cn.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.constants.Join;
import cn.intros.mybatis.plugin.sql.constants.Keywords;
import cn.intros.mybatis.plugin.sql.expression.Expression;

import java.io.IOException;
import java.util.List;

/**
 * Select sql
 *
 * @author teddy
 */
public class Select extends Sql<Select> {

    private static MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    private boolean first = true;

    public Select() {
        append(Keywords.KW_SELECT);
    }

    /**
     * @param columns
     * @return
     */
    public Select columns(String... columns) {
        if (first) {
            first = false;
            return Joiner.join(this, Keywords.COMMA_WITH_SPACE, columns);
        } else {
            return Joiner.join(this.append(Keywords.COMMA_WITH_SPACE), Keywords.COMMA_WITH_SPACE, columns);
        }
    }

    /**
     * @param expressions
     * @return
     */
    public Select columns(List<? extends Expression<Select>> expressions) {
        if (first) {
            first = false;
            return Joiner.join(this, Keywords.COMMA_WITH_SPACE, expressions);
        } else {
            return Joiner.join(this.append(Keywords.COMMA_WITH_SPACE), Keywords.COMMA_WITH_SPACE, expressions);
        }
    }

    /**
     * @param expressions
     * @return
     */
    public Select columns(Expression<Select>... expressions) {
        if (first) {
            first = false;
            return Joiner.join(this, Keywords.COMMA_WITH_SPACE, expressions);
        } else {
            return Joiner.join(this.append(Keywords.COMMA_WITH_SPACE), Keywords.COMMA_WITH_SPACE, expressions);
        }
    }

    /**
     * @param table
     * @return
     */
    public Select from(String table) {
        return this.append(Keywords.KW_FROM).append(table);
    }

    /**
     * @param table
     * @return
     */
    public Select from(Table<Select> table) {
        return table.write(this.append(Keywords.KW_FROM));
    }

    /**
     * @param table
     * @return
     * @throws IOException
     */
    public Select join(String table) {
        return join(table, Join.INNER);
    }


    /**
     * @param table
     * @param join
     * @return
     * @throws IOException
     */
    public Select join(String table, Join join) {
        return this.append(join.join()).append(table);
    }

    /**
     * @param table
     * @return
     */
    public Select join(Table<Select> table) {
        return join(table, Join.INNER);
    }


    /**
     * @param table
     * @param join
     * @return
     */
    public Select join(Table<Select> table, Join join) {
        return table.write(this.append(join.join()));
    }

    /**
     * @param condition
     * @return
     * @throws IOException
     */
    public Select on(Condition<Select> condition) {
        return condition.write(this.append(Keywords.KW_ON));
    }

    /**
     * group clause
     *
     * @param column
     * @return
     */
    public Select group(String... column) {
        return Joiner.join(this.append(Keywords.KW_GROUP_BY), Keywords.COMMA_WITH_SPACE, column);
    }

    /**
     * having clause
     *
     * @param condition
     * @return
     */
    public Select having(Condition<Select> condition) {
        return condition.write(this.append(Keywords.KW_HAVING));
    }

    /**
     * @param orders
     * @return
     */
    public Select order(Order<Select>... orders) {
        return Joiner.join(this.append(Keywords.KW_ORDER_BY), Keywords.COMMA_WITH_SPACE, orders);
    }

    /**
     * @param orders
     * @return
     */
    public Select order(List<Order<Select>> orders) {
        return Joiner.join(this.append(Keywords.KW_ORDER_BY), Keywords.COMMA_WITH_SPACE, orders);
    }

    /**
     * where clause
     *
     * @param condition
     * @return
     */
    public Select where(Condition<Select> condition) {
        return condition == null ? this : condition.write(this.append(Keywords.KW_WHERE));
    }

    /**
     * @param limit
     * @return
     */
    public Select limit(int limit) {
        return append(Keywords.KW_LIMIT).append(limit);
    }

    /**
     * @param offset
     * @return
     */
    public Select offset(int offset) {
        return append(Keywords.KW_OFFSET).append(offset);
    }
}
