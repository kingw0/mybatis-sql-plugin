package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import cn.intros.mybatis.plugin.mapping.ColumnInfo;
import cn.intros.mybatis.plugin.mapping.CriterionInfo;
import cn.intros.mybatis.plugin.sql.Update;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.constants.Keywords;
import cn.intros.mybatis.plugin.sql.expression.Binder;
import cn.intros.mybatis.plugin.sql.expression.Expression;
import cn.intros.mybatis.plugin.utils.ParameterUtils;
import cn.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static cn.intros.mybatis.plugin.sql.expression.Binder.bindIndexProp;
import static cn.intros.mybatis.plugin.sql.expression.Binder.bindProp;

/**
 * Generate update sql.
 *
 * <p>
 * Auto generate update sql from mapper method based on Column, Criterion annotation.
 * <p>
 * Column annotation can be on method, parameter, or field.
 * <p>
 *
 * @author teddy
 */
// TODO: 2020/9/2 support judge param value
public class UpdateSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSqlGenerator.class);

    private Collection<ColumnInfo> columnInfos;

    private Collection<CriterionInfo> criterionInfos;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public UpdateSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            this.columnInfos = this.columns.values();
            this.criterionInfos = this.criteria.values();
        }
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return update(context, paramObject);
    }

    private void multiUpdateColumns(Update update, Object element, int index, Collection<ColumnInfo> columnInfos) {
        for (ColumnInfo columnInfo : columnInfos) {
            if (shouldUpdate(columnInfo, element)) {
                update.set(columnInfo.column(), StringUtils.isNotBlank(columnInfo.expression())
                        ? Expression.expression(columnInfo.expression()) : Binder.bindIndexProp(columnInfo.parameter(), index, columnInfo.prop()));
            }
        }
    }

    private void updateColumns(Update update, Object paramObject, Collection<ColumnInfo> columnInfos) {
        for (ColumnInfo columnInfo : columnInfos) {
            if (shouldUpdate(columnInfo, paramObject)) {
                update.set(columnInfo.column(), StringUtils.isNotBlank(columnInfo.expression())
                        ? Expression.expression(columnInfo.expression()) : Binder.bindProp(columnInfo.parameter(), columnInfo.prop()));
            }
        }
    }

    private void buildMultiConditions(Update update, Object element, int size, int index,
                                      Collection<CriterionInfo> criterionInfos) {
        Optional<Condition> condition = criterionInfos.stream()
                .map(criterionInfo -> condition(criterionInfo, size, index, element))
                .filter(Objects::nonNull)
                .reduce((c1, c2) -> c1.and(c2));

        if (condition.isPresent()) {
            update.where(condition.get());
        }
    }

    private String update(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate update sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        Update update = new Update(table);

        if (multiQuery) {
            // multi update sql
            String paramName = this.paramNames[this.mappingParamIndex];

            Object paramValue = paramValue(paramObject, paramName);

            Collection<Object> collection = ParameterUtils.collection(paramValue);

            int index = 0, size = collection.size();

            Update additional;

            for (Object element : collection) {
                if (index == 0) {
                    multiUpdateColumns(update, element, index, columnInfos);

                    Optional<Condition> condition = conditions(criterionInfos, element, size, index);

                    if (condition.isPresent()) {
                        update.where(condition.get());
                    }
                } else {
                    additional = new Update(table);

                    multiUpdateColumns(additional, element, index, columnInfos);

                    Optional<Condition> condition = conditions(criterionInfos, element, size, index);

                    if (condition.isPresent()) {
                        additional.where(condition.get());
                    }

                    update.append(Keywords.SEMICOLON_WITH_SPACE).append(additional);
                }

                index++;
            }
        } else {
            updateColumns(update, paramObject, columnInfos);

            Optional<Condition> condition = conditions(criterionInfos, paramObject);

            if (condition.isPresent()) {
                update.where(condition.get());
            }
        }

        String sql = update.toString();

        LOGGER.debug("Generate update statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    private boolean shouldUpdate(ColumnInfo columnInfo, Object root) {
        return columnInfo.update() && test(columnInfo.test(), root);
    }
}
