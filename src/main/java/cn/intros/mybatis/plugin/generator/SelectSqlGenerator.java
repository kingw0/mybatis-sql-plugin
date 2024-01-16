package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import cn.intros.mybatis.plugin.annotation.Sort;
import cn.intros.mybatis.plugin.annotation.Sorts;
import cn.intros.mybatis.plugin.mapping.ColumnInfo;
import cn.intros.mybatis.plugin.sql.Order;
import cn.intros.mybatis.plugin.sql.Pageable;
import cn.intros.mybatis.plugin.sql.Select;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.expression.Column;
import cn.intros.mybatis.plugin.sql.expression.Expression;
import cn.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static cn.intros.mybatis.plugin.sql.Table.table;
import static cn.intros.mybatis.plugin.sql.expression.Column.column;

public class SelectSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSqlGenerator.class);
    private List<Expression<Select>> columnList;
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
                               ? Expression.expression(columnInfo.expression()) :
                               Column.column(this.alias, columnInfo.column()).as(columnInfo.prop()));
            }

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
                                                      ? Expression.expression(sort.expression()) : column(sort.column())));
                    } else {
                        orders.add(Order.<Select>asc(StringUtils.isNotBlank(sort.expression())
                                                     ? Expression.expression(sort.expression()) : column(sort.column())));
                    }
                }

                sortable = true;
            } else if (mapperMethod.isAnnotationPresent(Sort.class)) {
                Sort sort = mapperMethod.getAnnotation(Sort.class);

                if ("desc".equalsIgnoreCase(sort.order())) {
                    orders.add(Order.<Select>desc(StringUtils.isNotBlank(sort.expression())
                                                  ? Expression.expression(sort.expression()) : column(sort.column())));
                } else {
                    orders.add(Order.<Select>asc(StringUtils.isNotBlank(sort.expression())
                                                 ? Expression.expression(sort.expression()) : column(sort.column())));
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
        Select select = new Select().columns(this.columnList).from(table(this.table).as(this.alias));

        Optional<Condition> condition = conditions(this.criteria, paramObject);

        if (condition.isPresent()) {
            select.where(condition.get());
        }

        if (sortable) {
            select.order(orders);
        }

        if (pageable) {
            Pageable pageable = (Pageable) paramValue(paramObject, pageableParamName);

            if (pageable != null) {
                if (pageable.limit() > -1) {
                    select.limit(pageable.limit());
                }

                if (pageable.offset() > -1) {
                    select.offset(pageable.offset());
                }
            }
        }

        return select.toString();
    }
}
