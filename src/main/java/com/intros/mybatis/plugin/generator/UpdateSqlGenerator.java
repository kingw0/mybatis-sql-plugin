package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.sql.Joiner;
import com.intros.mybatis.plugin.sql.Update;
import com.intros.mybatis.plugin.utils.MappingUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static com.intros.mybatis.plugin.sql.constants.Keywords.*;
import static com.intros.mybatis.plugin.sql.expression.Bind.bind;

public class UpdateSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSqlGenerator.class);

    private static final int MAX_MULTI_SIZE = 256;

    private Predicate<ColumnInfo> updateColumnFilter;

    private Update update;

    private List<Update> updates = new ArrayList<>(MAX_MULTI_SIZE);

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public UpdateSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            updateColumnFilter = columnInfo -> columnInfo.update() ? (options == null ? true : !columnInfo.prop().equals(options.keyProperty())) : false;

            List<String> columns = MappingUtils.list(this.mappingClass, ColumnInfo::column, updateColumnFilter);

            List<String> props = MappingUtils.list(this.mappingClass, ColumnInfo::prop, updateColumnFilter);

            if (multiQuery) {
                String paramStart = this.paramNames[0] + OPEN_SQUARE_BRACKET;

                for (int i = 0; i < MAX_MULTI_SIZE; i++) {
                    String paramName = paramStart + i + CLOSE_SQUARE_BRACKET;
                    updates.add(update(this.mappingInfo.table(), columns, paramName, props).where(mappingCondition(paramName)));
                }
            } else {
                String paramName = this.hasParamAnnotation ? this.paramNames[0] : null;

                update = update(this.mappingInfo.table(), columns, paramName, props).where(mappingCondition(paramName));
            }
        }
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return buildUpdate(context, paramObject);


    }

    private Update update(String table, List<String> columns, String paramName, List<String> props) {
        if (columns.size() != props.size()) {
            throw new IllegalArgumentException("The size of columns must be the same as props.");
        }

        Update update = new Update(table);

        for (int i = 0, size = columns.size(); i < size; i++) {
            update.set(columns.get(i), bind(paramName, Arrays.asList(props.get(i))));
        }

        return update;
    }

    private String buildUpdate(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate update sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        String sql = "";

        if (multiQuery) {
            int size = paramSize(extractParam(paramObject, this.paramNames[0]), this.mapperMethodParams[0].getType());

            sql = Joiner.join(updates.subList(0, size), SEMICOLON_WITH_SPACE);
        } else {
            sql = update.toString();
        }

        LOGGER.debug("Generate update statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }
}
