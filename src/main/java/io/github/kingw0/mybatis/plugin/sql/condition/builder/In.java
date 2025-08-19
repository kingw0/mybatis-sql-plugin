package io.github.kingw0.mybatis.plugin.sql.condition.builder;

import io.github.kingw0.mybatis.plugin.mapping.CriterionInfo;
import io.github.kingw0.mybatis.plugin.sql.condition.Condition;
import io.github.kingw0.mybatis.plugin.sql.expression.Binder;
import io.github.kingw0.mybatis.plugin.sql.expression.Column;
import io.github.kingw0.mybatis.plugin.utils.ParameterUtils;

import java.util.Collection;

public class In extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        Collection<Object> collection = ParameterUtils.collection(paramValue);
        return Column.column(criterionInfo.column()).in(Binder.bindIndices(criterionInfo.parameter(),
                collection.size()));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject,
                           Object paramValue) {
        return super.build(criterionInfo, batchSize, indexInBatch, paramObject, paramValue);
    }
}
