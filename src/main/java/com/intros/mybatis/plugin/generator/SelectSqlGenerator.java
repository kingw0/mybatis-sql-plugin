package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.Order;
import com.intros.mybatis.plugin.sql.Pageable;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.Sorts;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import com.intros.mybatis.plugin.sql.expression.Column;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.intros.mybatis.plugin.sql.Order.asc;
import static com.intros.mybatis.plugin.sql.expression.Column.column;
import static java.util.stream.Collectors.toList;

public class SelectSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSqlGenerator.class);
    private List<Column<Select>> columnList;
    private Collection<CriterionInfo> criterionInfos;
    private String pageableParamName;
    private String sortableParamName;
    private boolean pageable;
    private boolean sortable;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public SelectSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            this.columnList = new LinkedList<>();

            for (ColumnInfo columnInfo : this.columns.values()) {
                columnList.add(Column.column(columnInfo.column()).as(columnInfo.prop()));
            }

            this.criterionInfos = this.criteria.values();

            for (int i = 0, len = this.parameters.length; i < len; i++) {
                Class<?> paramClass = this.parameters[i].getType();
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

        Select select = new Select().columns(this.columnList).from(this.table);

        Optional<Condition> condition = criterionInfos.stream()
                .map(criterionInfo -> condition(criterionInfo, paramValue(paramObject, criterionInfo.parameter())))
                .filter(Objects::nonNull)
                .reduce((c1, c2) -> c1.and(c2));

        if (condition.isPresent()) {
            select.where(condition.get());
        }

        if (sortable) {
            Sorts sorts = (Sorts) paramValue(paramObject, sortableParamName);

            if (sorts != null) {
                List<Order> orders =
                        sorts.sorts().stream().map(sort -> Keywords.KW_DESC.equals(sort.getOrder()) ? Order.desc(column(sort.getColumn())) : asc(column(sort.getColumn()))).collect(toList());

                select.order(orders.toArray(new Order[0]));
            }
        }

        if (pageable) {
            Pageable pageable = (Pageable) paramValue(paramObject, pageableParamName);

            if (pageable != null) {
                select.limit(pageable.limit()).offset(pageable.offset());
            }
        }

        String sql = select.toString();

        LOGGER.debug("Generate select statement[{}] for method[{}] of class[{}], params is [{}]!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

        return sql;
    }
}
