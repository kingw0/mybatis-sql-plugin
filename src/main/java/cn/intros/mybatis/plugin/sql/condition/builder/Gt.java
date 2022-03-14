package cn.intros.mybatis.plugin.sql.condition.builder;

import cn.intros.mybatis.plugin.mapping.CriterionInfo;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.expression.Binder;
import cn.intros.mybatis.plugin.sql.expression.Column;

import static cn.intros.mybatis.plugin.sql.expression.Binder.bindProp;
import static cn.intros.mybatis.plugin.sql.expression.Column.column;

public class Gt extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        return Column.column(criterionInfo.column()).gt(Binder.bindProp(criterionInfo.parameter(), criterionInfo.prop()));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject, Object paramValue) {
        return super.build(criterionInfo, batchSize, indexInBatch, paramObject, paramValue);
    }
}
