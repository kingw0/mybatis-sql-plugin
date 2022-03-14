package cn.intros.mybatis.plugin.sql.condition.builder;

import cn.intros.mybatis.plugin.mapping.CriterionInfo;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.expression.Binder;
import cn.intros.mybatis.plugin.sql.expression.Column;
import cn.intros.mybatis.plugin.utils.ParameterUtils;

import java.util.Collection;

import static cn.intros.mybatis.plugin.sql.expression.Binder.bindIndices;
import static cn.intros.mybatis.plugin.sql.expression.Column.column;

public class In extends Builder {
    @Override
    public Condition build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        Collection<Object> collection = ParameterUtils.collection(paramValue);
        return Column.column(criterionInfo.column()).in(Binder.bindIndices(criterionInfo.parameter(), collection.size()));
    }

    @Override
    public Condition build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject, Object paramValue) {
        return super.build(criterionInfo, batchSize, indexInBatch, paramObject, paramValue);
    }
}
