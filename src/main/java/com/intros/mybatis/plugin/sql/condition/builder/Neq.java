package com.intros.mybatis.plugin.sql.condition.builder;

import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.condition.Condition;

import static com.intros.mybatis.plugin.sql.expression.Binder.bindProp;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

public class Neq extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object root) {
        return column(criterionInfo.column()).neq(bindProp(criterionInfo.parameter(), criterionInfo.prop()));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object root) {
        return super.build(criterionInfo, batchSize, indexInBatch, root);
    }
}
