package com.intros.mybatis.plugin.sql.condition.builder;

import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.utils.StringUtils;

import static com.intros.mybatis.plugin.sql.expression.Binder.*;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

/**
 * Class to build {@link Condition} from {@link CriterionInfo} and param value
 *
 * @param <S>
 */
public class Builder<S extends Sql<S>> {
    /**
     * @param criterionInfo
     * @param root
     * @return
     */
    public Condition<S> build(CriterionInfo criterionInfo, Object root) {
        return column(criterionInfo.column()).eq(StringUtils.isBlank(criterionInfo.parameter()) ?
                bind(criterionInfo.prop()) : bindProp(criterionInfo.parameter(), criterionInfo.prop()));
    }

    /**
     * @param criterionInfo
     * @param batchSize
     * @param indexInBatch
     * @param root
     * @return
     */
    public Condition<S> build(CriterionInfo criterionInfo, int batchSize, int indexInBatch, Object root) {
        return column(criterionInfo.column()).eq(bindIndexProp(criterionInfo.parameter(), indexInBatch, criterionInfo.prop()));
    }
}
