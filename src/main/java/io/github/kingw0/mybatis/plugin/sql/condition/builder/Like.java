package io.github.kingw0.mybatis.plugin.sql.condition.builder;

import io.github.kingw0.mybatis.plugin.mapping.CriterionInfo;
import io.github.kingw0.mybatis.plugin.sql.condition.Condition;
import io.github.kingw0.mybatis.plugin.sql.expression.Binder;
import io.github.kingw0.mybatis.plugin.sql.expression.Column;
import io.github.kingw0.mybatis.plugin.sql.expression.Expression;

import static io.github.kingw0.mybatis.plugin.sql.expression.Literal.text;

public class Like extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        return Column.column(criterionInfo.column()).like(
                Expression.func("concat",
                        text("%"),
                        Binder.bindProp(criterionInfo.parameter(), criterionInfo.prop()),
                        text("%")));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject,
                           Object paramValue) {
        return super.build(criterionInfo, batchSize, indexInBatch, paramObject, paramValue);
    }
}
