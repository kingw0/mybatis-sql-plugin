package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.sql.*;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.sql.expression.Column;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.intros.mybatis.plugin.sql.Order.asc;
import static com.intros.mybatis.plugin.sql.Table.table;
import static com.intros.mybatis.plugin.sql.expression.Column.column;
import static java.util.stream.Collectors.toList;

public class SelectSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSqlGenerator.class);
    protected List<Column<Select>> columns;
    protected String pageableParamName;
    protected String sortableParamName;
    protected boolean pageable;
    protected boolean sortable;
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
                Class<?> paramClass = this.mapperMethodParams[i].getType();
                if (Pageable.class.isAssignableFrom(paramClass)) {
                    pageable = true;
                    pageableParamName = this.paramNames[i];
                    continue;
                }

                if (Sorts.class.isAssignableFrom(paramClass)) {
                    sortable = true;
                    sortableParamName = this.paramNames[i];
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

        if (sortable) {
            Sorts sorts = (Sorts) getArgByParamName(paramObject,
                    sortableParamName);

            if (sorts != null) {
                List<Order> orders =
                        sorts.sorts().stream().map(sort -> Keywords.KW_DESC.equals(sort.getOrder()) ? Order.desc(column(sort.getColumn())) : asc(column(sort.getColumn()))).collect(toList());

                select.order(orders.toArray(new Order[0]));
            }
        }

        if (pageable) {
            Pageable pageable = (Pageable) getArgByParamName(paramObject, pageableParamName);

            if (pageable != null) {
                select.limit(pageable.limit()).offset(pageable.offset());
            }
        }

        String sql = select.toString();

        LOGGER.debug("Generate select statement[{}] for method[{}] of class[{}], params is [{}]!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

        return sql;
    }
}
