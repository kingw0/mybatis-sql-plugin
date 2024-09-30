package cn.intros.mybatis.plugin.sql.expression;

import cn.intros.mybatis.plugin.sql.Joiner;
import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.constants.Keywords;

import java.util.Arrays;
import java.util.List;

/**
 * @param <S>
 * @author teddy
 */
public class ExpressionList<S extends Sql<S>> extends Expression<S> {
    
    private List<? extends Expression<S>> expressions;

    protected ExpressionList(List<? extends Expression<S>> expressions) {
        this.expressions = expressions;
    }

    public static <S extends Sql<S>> ExpressionList<S> list(List<? extends Expression<S>> expressions) {
        return new ExpressionList<S>(expressions);
    }

    public static <S extends Sql<S>> ExpressionList<S> list(Expression<S>... expressions) {
        return new ExpressionList<S>(Arrays.asList(expressions));
    }

    @Override
    public S write(S sql) {
        return Joiner.join(sql, Keywords.COMMA_WITH_SPACE, this.expressions);
    }
}
