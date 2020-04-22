package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.condition.Condition;

import static com.intros.mybatis.plugin.sql.constants.Keywords.KW_DELETE;
import static com.intros.mybatis.plugin.sql.constants.Keywords.KW_WHERE;

/**
 * @author teddy
 */
public class Delete extends SQL<Delete> {
    public Delete(String table) {
        append(KW_DELETE).append(table);
    }

    public Delete where(Condition<Delete> condition) {
        return condition.write(this.append(KW_WHERE));
    }
}
