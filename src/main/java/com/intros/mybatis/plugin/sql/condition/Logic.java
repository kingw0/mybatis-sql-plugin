package com.intros.mybatis.plugin.sql.condition;

import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.constants.LogicalConditionOp;
import com.intros.mybatis.plugin.sql.expression.Expression;

import static com.intros.mybatis.plugin.sql.constants.LogicalConditionOp.NOT;

public class Logic<S extends Sql<S>> extends Condition<S> {
    private LogicalConditionOp op;

    private Expression<S> expr;

    protected Logic(LogicalConditionOp op, Expression<S> expr) {
        this.op = op;
        this.expr = expr;
    }

    public static <S extends Sql<S>> Logic<S> not(Expression<S> expr) {
        return new Logic<>(NOT, expr);
    }

    @Override
    public S write(S sql) {
        return this.expr.write(sql.append(op.op()));
    }
}
