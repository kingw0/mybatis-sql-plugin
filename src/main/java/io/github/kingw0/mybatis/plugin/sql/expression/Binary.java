package io.github.kingw0.mybatis.plugin.sql.expression;

import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.constants.BinaryExpressionOp;

import static io.github.kingw0.mybatis.plugin.sql.constants.BinaryExpressionOp.*;

/**
 * @param <S> type of SQL
 * @author teddy
 */
public class Binary<S extends Sql<S>> extends Expression<S> {
    private Expression<S> left;

    private Expression<S> right;

    private BinaryExpressionOp op;

    protected Binary(Expression<S> left, BinaryExpressionOp op, Expression<S> right) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public static <S extends Sql<S>> Binary<S> add(Expression<S> left, Expression<S> right) {
        return new Binary<>(left, ADDITION, right);
    }

    public static <S extends Sql<S>> Binary<S> sub(Expression<S> left, Expression<S> right) {
        return new Binary<>(left, SUBTRACTION, right);
    }

    public static <S extends Sql<S>> Binary<S> mul(Expression<S> left, Expression<S> right) {
        return new Binary<>(left, MULTIPLICATION, right);
    }

    public static <S extends Sql<S>> Binary<S> div(Expression<S> left, Expression<S> right) {
        return new Binary<>(left, DIVISION, right);
    }

    @Override
    public S write(S sql) {
        return right.write(left.write(sql).append(op.op()));
    }
}
