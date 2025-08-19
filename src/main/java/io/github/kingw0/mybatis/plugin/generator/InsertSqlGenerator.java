package io.github.kingw0.mybatis.plugin.generator;

import io.github.kingw0.mybatis.plugin.SqlType;
import io.github.kingw0.mybatis.plugin.mapping.ColumnInfo;
import io.github.kingw0.mybatis.plugin.sql.Insert;
import io.github.kingw0.mybatis.plugin.sql.expression.Binder;
import io.github.kingw0.mybatis.plugin.sql.expression.Expression;
import io.github.kingw0.mybatis.plugin.utils.ParameterUtils;
import io.github.kingw0.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InsertSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSqlGenerator.class);

    private Options options;

    private Collection<ColumnInfo> columnInfos;

    private Map<String, Boolean> cachedCanInsert;

    public InsertSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);

        if (!hasProvider) {
            // Get options annotation for insert sql statement
            if (context.getMapperMethod().isAnnotationPresent(Options.class)) {
                options = context.getMapperMethod().getAnnotation(Options.class);
            }

            this.columnInfos = this.columns.values();

            cachedCanInsert = new HashMap<>();

            this.columnInfos.forEach(columnInfo -> {
                cachedCanInsert.put(columnInfo.column(), canInsert(columnInfo));
            });
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
        Insert insert = new Insert(this.table);

        if (multiQuery) {
            String paramName = this.paramNames[this.mappingParamIndex];

            Object paramValue = paramValue(paramObject, paramName);

            Collection<Object> collection = ParameterUtils.collection(paramValue);

            List<String> columnList = new LinkedList<>();

            for (ColumnInfo columnInfo : this.columnInfos) {
                if (cachedCanInsert.get(columnInfo.column())) {
                    columnList.add(columnInfo.column());
                }
            }

            insert.columns(columnList);

            int index = 0, size = collection.size();

            while (index < size) {
                List<Expression<Insert>> expressions = new LinkedList<>();

                for (ColumnInfo columnInfo : this.columnInfos) {
                    if (cachedCanInsert.get(columnInfo.column())) {
                        expressions.add(StringUtils.isNotBlank(columnInfo.expression())
                                        ? Expression.expression(columnInfo.expression()) :
                                        Binder.bindIndexProp(columnInfo.parameter(), index, columnInfo.prop()));
                    }
                }

                insert.values(expressions);

                index++;
            }
        } else {
            List<String> columnList = new LinkedList<>();

            for (ColumnInfo columnInfo : this.columnInfos) {
                if (shouldInsert(columnInfo, paramObject)) {
                    columnList.add(columnInfo.column());
                }
            }

            insert.columns(columnList);

            List<Expression<Insert>> expressions = new LinkedList<>();

            for (ColumnInfo columnInfo : this.columnInfos) {
                if (shouldInsert(columnInfo, paramObject)) {
                    expressions.add(StringUtils.isNotBlank(columnInfo.expression())
                                    ? Expression.expression(columnInfo.expression()) : Binder.bindProp(columnInfo.parameter()
                        , columnInfo.prop()));
                }
            }

            insert.values(expressions);
        }

        return insert.toString();
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

    private boolean shouldInsert(ColumnInfo columnInfo, Object root) {

        if (!cachedCanInsert.get(columnInfo.column())) {
            return false;
        }

        if (!columnInfo.insertNull()) {
            String prop = columnInfo.parameter();

            if (StringUtils.isBlank(prop)) {
                prop = columnInfo.prop();
            }

            if (StringUtils.isNotBlank(prop) && OgnlCache.getValue(prop, root) == null) {
                return false;
            }
        }

        return test(columnInfo.test(), root);
    }
}
