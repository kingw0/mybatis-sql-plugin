package com.intros.mybatis.plugin.sql.condition.builder;

import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.condition.Condition;

import static com.intros.mybatis.plugin.sql.expression.Binder.bindProp;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

public class Gt extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        return column(criterionInfo.column()).gt(bindProp(criterionInfo.parameter(), criterionInfo.prop()));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject, Object paramValue) {
        return super.build(criterionInfo, batchSize, indexInBatch, paramObject, paramValue);
    }
}
