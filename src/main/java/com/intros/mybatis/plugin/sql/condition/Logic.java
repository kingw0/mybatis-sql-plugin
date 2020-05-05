package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.LogicalConditionOp;
import com.intros.mybatis.plugin.sql.expression.Expression;

public class Logic<S extends Sql<S>> extends Condition<S> {
    private static final Class<Logic> THIS_CLASS = Logic.class;

    static {
        registerFactory(THIS_CLASS, initArgs -> new Logic((LogicalConditionOp) initArgs[0], (Expression) initArgs[1]));
    }

    private LogicalConditionOp op;

    private Expression<S> expr;

    protected Logic(LogicalConditionOp op, Expression<S> expr) {
        this.op = op;
        this.expr = expr;
    }

    public static <S extends Sql<S>> Logic<S> not(Expression<S> expr) {
        return instance(THIS_CLASS, LogicalConditionOp.NOT, expr);
    }

    @Override
    public S write(S sql) {
        return this.expr.write(sql.append(op.op()));
    }
}
