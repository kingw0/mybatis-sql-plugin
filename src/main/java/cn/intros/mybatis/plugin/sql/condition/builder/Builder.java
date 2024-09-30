package cn.intros.mybatis.plugin.sql.condition.builder;

import cn.intros.mybatis.plugin.mapping.CriterionInfo;
import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.expression.Binder;
import cn.intros.mybatis.plugin.sql.expression.Column;

import static cn.intros.mybatis.plugin.sql.expression.Binder.bindIndexProp;
import static cn.intros.mybatis.plugin.sql.expression.Binder.bindProp;
import static cn.intros.mybatis.plugin.sql.expression.Column.column;

/**
 * Class to build {@link Condition} from {@link CriterionInfo} and param value
 *
 * @param <S>
 */
public class Builder<S extends Sql<S>> {
    /**
     * @param criterionInfo
     * @param paramObject
     * @param paramValue
     * @return
     */
    public Condition<S> build(CriterionInfo criterionInfo, Object paramObject, Object paramValue) {
        return Column.column(criterionInfo.column()).eq(Binder.bindProp(criterionInfo.parameter(), criterionInfo.prop()));
    }

    /**
     * @param criterionInfo
     * @param batchSize
     * @param indexInBatch
     * @return
     */
    public Condition<S> build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object paramObject, Object paramValue) {
        return Column.column(criterionInfo.column()).eq(Binder.bindIndexProp(criterionInfo.parameter(), indexInBatch, criterionInfo.prop()));
    }
}
