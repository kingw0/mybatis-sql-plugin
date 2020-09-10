package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.sql.Insert;
import com.intros.mybatis.plugin.sql.Table;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static com.intros.mybatis.plugin.sql.Table.table;
import static com.intros.mybatis.plugin.sql.expression.Binder.bindIndexProps;
import static com.intros.mybatis.plugin.sql.expression.Binder.bindMultiProps;
import static com.intros.mybatis.plugin.utils.ParameterUtils.sizeOfParam;

public class InsertSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSqlGenerator.class);

    private Table table;

    private List<ColumnInfo> columnInfos;

    private String[] columns;

    private String[] props;

    public InsertSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            table = table(this.mappingClass);
            columnInfos = this.mappingInfo.columnInfos();

            List<ColumnInfo> filteredColumnInfos = this.columnInfos.stream().filter(this::canInsert).collect(Collectors.toList());

            int filteredSize = filteredColumnInfos.size();

            this.columns = filteredColumnInfos.stream().map(ColumnInfo::column).collect(Collectors.toList()).toArray(new String[filteredSize]);

            this.props = filteredColumnInfos.stream().map(ColumnInfo::prop).collect(Collectors.toList()).toArray(new String[filteredSize]);
        }
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return insert(context, paramObject);
    }

    /**
     * build insert sql for mapper method
     *
     * @param context
     * @param paramObject
     * @return
     */
    private String insert(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate insert sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        String sql;

        Insert insert = new Insert(this.table);

        String paramName = multiQuery ? this.paramNames[0] : this.hasParamAnnotation ? this.paramNames[0] : null;

        insert.columns(this.columns);

        if (multiQuery) {
            int index = 0, size = sizeOfParam(getArgByParamName(paramObject, this.paramNames[0]));

            insert.values(bindIndexProps(paramName, index++, this.props));

            for (; index < size; index++) {
                insert.append(bindIndexProps(paramName, index, this.props));
            }
        } else {
            insert.values(bindMultiProps(paramName, this.props));
        }

        sql = insert.toString();

        LOGGER.debug("Generate insert statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    private boolean canInsert(ColumnInfo columnInfo) {
        if (!columnInfo.insert()) {
            return false;
        }

        if (options != null && options.useGeneratedKeys() && columnInfo.prop().equals(options.keyProperty())) {
            return false;
        }

        return true;
    }
}
