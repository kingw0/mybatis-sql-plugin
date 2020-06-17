package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.sql.Pageable;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.expression.Column;
import com.intros.mybatis.plugin.utils.Mapping;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SelectSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSqlGenerator.class);

    protected List<Column<Select>> columns;

    protected String pageableParam;

    protected boolean isPageable;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public SelectSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            columns = Mapping.columns(mappingClass, true);

            for (int i = 0, len = this.mapperMethodParams.length; i < len; i++) {
                if (Pageable.class.isAssignableFrom(this.mapperMethodParams[i].getType())) {
                    isPageable = true;
                    pageableParam = this.paramNames[i];
                    break;
                }
            }
        }
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return buildSelect(context, paramObject);
    }

    private String buildSelect(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate select sql for method[{}] of class[{}].", context.getMapperMethod(), context.getMapperType());

        Select select = new Select().columns(columns).from(this.mappingInfo.table()).where(prepareCondition(paramObject));

        if (isPageable) {
            Pageable pageable = (Pageable) extractParam(paramObject, pageableParam);
            select.limit(pageable.limit()).offset(pageable.offset());
        }

        String sql = select.toString();

        LOGGER.debug("Generate select statement[{}] for method[{}] of class[{}], params is [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }
}
