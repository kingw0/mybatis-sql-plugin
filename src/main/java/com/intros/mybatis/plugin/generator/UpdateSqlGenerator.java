package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.sql.Table;
import com.intros.mybatis.plugin.sql.Update;
import com.intros.mybatis.plugin.sql.constants.Keywords;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.intros.mybatis.plugin.sql.Table.table;
import static com.intros.mybatis.plugin.sql.expression.Binder.bindIndexProp;
import static com.intros.mybatis.plugin.sql.expression.Binder.bindProp;
import static com.intros.mybatis.plugin.utils.ParameterUtils.sizeOfParam;
import static com.intros.mybatis.plugin.utils.ParameterUtils.specificValueInParam;

/**
 * @author teddy
 */
// TODO: 2020/9/2 support judge param value
public class UpdateSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSqlGenerator.class);

    Table table;

    private List<ColumnInfo> columnInfos;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public UpdateSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            table = table(this.mappingClass);
            columnInfos = this.mappingInfo.columnInfos();
        }
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return buildUpdate(context, paramObject);
    }

    private String buildUpdate(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate update sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        Object arg = getArgByParamName(paramObject, this.paramNames[0]);

        Update update = new Update(table);

        String paramName = multiQuery ? this.paramNames[0] : this.hasParamAnnotation ? this.paramNames[0] : null;

        if (multiQuery) {
            int index = 0, size = sizeOfParam(arg);

            updateColumns(update, arg, paramName, index++, this.columnInfos);

            for (; index < size; index++) {
                update.append(Keywords.SEMICOLON_WITH_SPACE).append(updateColumns(new Update(table), arg, paramName, index, this.columnInfos));
            }
        } else {
            updateColumns(update, arg, paramName, this.columnInfos);
        }

        String sql = update.toString();

        LOGGER.debug("Generate update statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    private Update updateColumns(Update update, Object param, String paramName, List<ColumnInfo> columnInfos) {
        for (ColumnInfo columnInfo : columnInfos) {
            if (canUpdate(param, columnInfo)) {
                update.set(columnInfo.column(), bindProp(paramName, columnInfo.prop()));
            }
        }

        return update.where(queryCondByKeyProperty(paramName));
    }

    private Update updateColumns(Update update, Object param, String paramName, int index, List<ColumnInfo> columnInfos) {
        for (ColumnInfo columnInfo : columnInfos) {
            if (canUpdate(specificValueInParam(param, index), columnInfo)) {
                update.set(columnInfo.column(), bindIndexProp(paramName, index, columnInfo.prop()));
            }
        }

        return update.where(queryCondByKeyProperty(paramName, index));
    }

    private boolean canUpdate(Object target, ColumnInfo columnInfo) {
        if (!columnInfo.update()) {
            return false;
        }

        if (options != null && columnInfo.prop().equals(options.keyProperty())) {
            return false;
        }

        if (!columnInfo.nullable()) {
            try {
                return columnInfo.getValue(target) != null;
            } catch (ReflectiveOperationException e) {
                // warn, update
            }
        }

        return true;
    }
}
