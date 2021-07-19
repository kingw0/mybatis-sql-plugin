package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.sql.Pageable;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.Table;
import com.intros.mybatis.plugin.sql.expression.Column;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.intros.mybatis.plugin.sql.Table.table;

public class SelectSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSqlGenerator.class);
    protected List<Column<Select>> columns;
    protected String pageableParamName;
    protected boolean pageable;
    private Table<Select> table;
    private List<ColumnInfo> columnInfos;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public SelectSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            this.table = table(this.mappingClass);

            this.columnInfos = this.mappingInfo.columnInfos();

            this.columns = new ArrayList<>(columnInfos.size());

            for (ColumnInfo columnInfo : this.columnInfos) {
                this.columns.add(table.column(columnInfo.column()).as(columnInfo.prop()));
            }

            for (int i = 0, len = this.mapperMethodParams.length; i < len; i++) {
                if (Pageable.class.isAssignableFrom(this.mapperMethodParams[i].getType())) {
                    pageable = true;
                    pageableParamName = this.paramNames[i];
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

        Select select = new Select().columns(this.columns).from(this.table).where(queryCondByCriteria(paramObject));

        if (pageable) {
            Pageable pageable = (Pageable) getArgByParamName(paramObject, pageableParamName);
            select.limit(pageable.limit()).offset(pageable.offset());
        }

        String sql = select.toString();

        LOGGER.debug("Generate select statement[{}] for method[{}] of class[{}], params is [{}]!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

        return sql;
    }
}
