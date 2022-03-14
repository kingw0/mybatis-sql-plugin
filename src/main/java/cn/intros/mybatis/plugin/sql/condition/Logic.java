package cn.intros.mybatis.plugin.sql.condition;

import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.constants.LogicalConditionOp;

import static cn.intros.mybatis.plugin.sql.constants.LogicalConditionOp.NOT;

public class Logic<S extends Sql<S>> extends Condition<S> {
    private LogicalConditionOp op;

    private Condition<S> cond;

    protected Logic(LogicalConditionOp op, Condition<S> cond) {
        this.op = op;
        this.cond = cond;
    }

    public static <S extends Sql<S>> Logic<S> not(Condition<S> cond) {
        return new Logic<>(NOT, cond);
    }

    @Override
    public S write(S sql) {
        return this.cond.write(sql.append(op.op()));
    }
}
