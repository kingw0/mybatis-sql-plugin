package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.Sort;
import com.intros.mybatis.plugin.annotation.Sorts;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.Order;
import com.intros.mybatis.plugin.sql.Pageable;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.expression.Expression;
import com.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

import static com.intros.mybatis.plugin.sql.expression.Column.column;
import static com.intros.mybatis.plugin.sql.expression.Expression.expression;

public class SelectSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSqlGenerator.class);
    private List<Expression<Select>> columnList;
    private Collection<CriterionInfo> criterionInfos;
    private String pageableParamName;
    private boolean pageable;
    private boolean sortable;
    private List<Order<Select>> orders = new LinkedList<>();

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
                columnList.add(StringUtils.isNotBlank(columnInfo.expression())
                        ? expression(columnInfo.expression()) : column(columnInfo.column()).as(columnInfo.prop()));
            }

            this.criterionInfos = this.criteria.values();

            for (int i = 0, len = this.parameters.length; i < len; i++) {
                Class<?> paramClass = this.parameters[i].getType();
                if (Pageable.class.isAssignableFrom(paramClass)) {
                    pageable = true;
                    pageableParamName = this.paramNames[i];
                    break;
                }
            }

            Method mapperMethod = context.getMapperMethod();

            if (mapperMethod.isAnnotationPresent(Sorts.class)) {
                for (Sort sort : mapperMethod.getAnnotation(Sorts.class).value()) {
                    if ("desc".equalsIgnoreCase(sort.order())) {
                        orders.add(Order.<Select>desc(StringUtils.isNotBlank(sort.expression())
                                ? expression(sort.expression()) : column(sort.column())));
                    } else {
                        orders.add(Order.<Select>asc(StringUtils.isNotBlank(sort.expression())
                                ? expression(sort.expression()) : column(sort.column())));
                    }
                }

                sortable = true;
            } else if (mapperMethod.isAnnotationPresent(Sort.class)) {
                Sort sort = mapperMethod.getAnnotation(Sort.class);

                if ("desc".equalsIgnoreCase(sort.order())) {
                    orders.add(Order.<Select>desc(StringUtils.isNotBlank(sort.expression())
                            ? expression(sort.expression()) : column(sort.column())));
                } else {
                    orders.add(Order.<Select>asc(StringUtils.isNotBlank(sort.expression())
                            ? expression(sort.expression()) : column(sort.column())));
                }

                sortable = true;
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
                .map(criterionInfo -> condition(criterionInfo, StringUtils.isNotBlank(criterionInfo.parameter())
                        ? paramValue(paramObject, criterionInfo.parameter()) : null))
                .filter(Objects::nonNull)
                .reduce((c1, c2) -> c1.and(c2));

        if (condition.isPresent()) {
            select.where(condition.get());
        }

        if (sortable) {
            select.order(orders);
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
