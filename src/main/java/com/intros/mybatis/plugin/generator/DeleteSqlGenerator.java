package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.sql.Delete;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class DeleteSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSqlGenerator.class);

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public DeleteSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return buildDelete(context, paramObject);
    }

    private String buildDelete(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate delete sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        Delete delete = new Delete(this.table);

        Optional<Condition> condition = this.criteria.values().stream()
                .map(criterionInfo -> condition(criterionInfo, StringUtils.isNotBlank(criterionInfo.parameter())
                        ? paramValue(paramObject, criterionInfo.parameter()) : null))
                .filter(Objects::nonNull)
                .reduce((c1, c2) -> c1.and(c2));

        if (condition.isPresent()) {
            delete.where(condition.get());
        }

        String sql = delete.toString();

        LOGGER.debug("Generate delete statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }
}
