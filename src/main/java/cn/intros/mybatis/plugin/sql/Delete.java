package cn.intros.mybatis.plugin.sql;

import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.constants.Keywords;

/**
 * @author teddy
 */
public class Delete extends Sql<Delete> {
    public Delete(String table) {
        append(Keywords.KW_DELETE).append(table);
    }

    public Delete(Table table) {
        append(Keywords.KW_DELETE).append(table);
    }

    public Delete where(Condition<Delete> condition) {
        return condition == null ? this : condition.write(this.append(Keywords.KW_WHERE));
    }
}
