package com.intros.mybatis.plugin.sql.condition.builder;

import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.condition.Condition;

import java.util.Collection;

import static com.intros.mybatis.plugin.sql.expression.Binder.bindIndices;
import static com.intros.mybatis.plugin.sql.expression.Column.column;
import static com.intros.mybatis.plugin.utils.ParameterUtils.collection;

public class In extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        Collection<Object> collection = collection(paramValue);
        return column(criterionInfo.column()).in(bindIndices(criterionInfo.parameter(), collection.size()));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject, Object paramValue) {
        return super.build(criterionInfo, batchSize, indexInBatch, paramObject, paramValue);
    }
}
