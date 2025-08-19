package io.github.kingw0.mybatis.plugin.sql.condition.builder;

import io.github.kingw0.mybatis.plugin.mapping.CriterionInfo;
import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.condition.Condition;
import io.github.kingw0.mybatis.plugin.sql.expression.Binder;
import io.github.kingw0.mybatis.plugin.sql.expression.Column;

import static io.github.kingw0.mybatis.plugin.sql.expression.Binder.bindIndexProp;
import static io.github.kingw0.mybatis.plugin.sql.expression.Binder.bindProp;
import static io.github.kingw0.mybatis.plugin.sql.expression.Column.column;

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
