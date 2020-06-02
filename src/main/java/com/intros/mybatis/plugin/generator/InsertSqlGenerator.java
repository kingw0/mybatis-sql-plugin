package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.sql.Insert;
import com.intros.mybatis.plugin.sql.expression.Bind;
import com.intros.mybatis.plugin.utils.MappingUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.intros.mybatis.plugin.sql.constants.Keywords.CLOSE_SQUARE_BRACKET;
import static com.intros.mybatis.plugin.sql.constants.Keywords.OPEN_SQUARE_BRACKET;
import static com.intros.mybatis.plugin.sql.expression.Bind.bind;

public class InsertSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSqlGenerator.class);

    private static final int MAX_MULTI_SIZE = 256;

    // insert sql column info filter
    private Predicate<ColumnInfo> insertColumnFilter;

    private Insert insert;
    private List<String> columns;
    private List<Bind<Insert>> binds = new ArrayList<>(MAX_MULTI_SIZE);

    public InsertSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            insertColumnFilter = columnInfo -> columnInfo.insert() ? (options == null ? true : !options.useGeneratedKeys() || !columnInfo.prop().equals(options.keyProperty())) : false;

            columns = MappingUtils.list(this.mappingClass, ColumnInfo::column, insertColumnFilter);

            List<String> props = MappingUtils.list(this.mappingClass, ColumnInfo::prop, insertColumnFilter);

            if (multiQuery) {
                String paramStart = this.paramNames[0] + OPEN_SQUARE_BRACKET;

                for (int i = 0; i < MAX_MULTI_SIZE; i++) {
                    binds.add(Bind.bind(paramStart + i + CLOSE_SQUARE_BRACKET, props));
                }
            } else {
                insert = insert(this.mappingInfo.table(), columns, this.hasParamAnnotation ? this.paramNames[0] : null, props);
            }
        }
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return insert(context, paramObject);
    }

    private Insert insert(String table, List<String> columns, String paramName, List<String> props) {
        if (columns.size() != props.size()) {
            throw new IllegalArgumentException("The size of columns must be the same as props.");
        }

        return new Insert(table).columns(columns).values(bind(paramName, props));
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

        String sql = "";

        if (multiQuery) {
            int size = paramSize(extractParam(paramObject, this.paramNames[0]), this.mapperMethodParams[0].getType());

            Insert insert = new Insert(this.mappingInfo.table()).columns(columns).values(binds.get(0));

            for (int i = 1; i < size; i++) {
                insert.append(binds.get(i));
            }

            sql = insert.toString();
        } else {
            sql = insert.toString();
        }

        LOGGER.debug("Generate insert statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }
}
