package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.SQL;
import com.intros.mybatis.plugin.sql.constants.BinaryExpressionOp;

/**
 * @param <S> type of SQL
 * @author teddy
 */
public class Binary<S extends SQL<S>> extends Expression<S> {
    private static final Class<Binary> THIS_CLASS = Binary.class;

    static {
        registerFactory(THIS_CLASS, (initArgs -> {
            if (initArgs.length == 3) {
                if (initArgs[0] instanceof Expression && initArgs[1] instanceof BinaryExpressionOp && initArgs[2] instanceof Expression) {
                    return new Binary((Expression) initArgs[0], (BinaryExpressionOp) initArgs[1], (Expression) initArgs[2]);
                }
            }

            return null;
        }));
    }

    private Expression<S> left;

    private Expression<S> right;

    private BinaryExpressionOp op;

    protected Binary(Expression<S> left, BinaryExpressionOp op, Expression<S> right) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public static <S extends SQL<S>> Binary<S> add(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, BinaryExpressionOp.ADDITION, right);
    }

    public static <S extends SQL<S>> Binary<S> sub(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, BinaryExpressionOp.SUBTRACTION, right);
    }

    public static <S extends SQL<S>> Binary<S> mul(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, BinaryExpressionOp.MULTIPLICATION, right);
    }

    public static <S extends SQL<S>> Binary<S> div(Expression<S> left, Expression<S> right) {
        return instance(THIS_CLASS, left, BinaryExpressionOp.DIVISION, right);
    }

    @Override
    public S write(S sql) {
        return right.write(left.write(sql).append(op.op()));
    }
}
