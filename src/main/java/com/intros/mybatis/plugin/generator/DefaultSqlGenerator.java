package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.annotation.Mapping;
import com.intros.mybatis.plugin.annotation.Provider;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.*;
import com.intros.mybatis.plugin.sql.condition.Comparison;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.utils.MappingUtils;
import com.intros.mybatis.plugin.utils.ReflectionUtils;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.intros.mybatis.plugin.sql.constants.BindType.BIND;
import static com.intros.mybatis.plugin.sql.constants.Keywords.OPEN_SQUARE_BRACKET;
import static com.intros.mybatis.plugin.sql.expression.Bind.bind;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

/**
 * Default Sql Generator, one generator instance for one mapper method
 *
 * <p>
 * 1. generate sql from provider which can be a default interface method or a class static method first;
 * 2. if no provider,
 * </p>
 *
 * @author teddy
 * @since 2019/08/23
 */
public class DefaultSqlGenerator implements SqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSqlGenerator.class);
    private static final int BATCH_INSERT_MAX_COUNT = 128;
    private static MappingInfoRegistry registry = MappingInfoRegistry.getInstance();
    private boolean hasParamAnnotation;
    private Parameter[] mapperMethodParams;
    private String[] paramNames;
    private Class<?> providerClass;
    private Method providerMethod;
    private MethodHandle methodHandle;
    // mapping info
    private Class<?> mappingClass;
    private MappingInfo mappingInfo;
    private Options options;
    // insert sql column info filter
    private final Predicate<ColumnInfo> INSERT_PREDICATE = columnInfo -> options == null ? columnInfo.insert() :
            !options.useGeneratedKeys() || !columnInfo.prop().equals(options.keyProperty());
    // sql type of this mapper method
    private SqlType sqlType;
    // cached sql
    private String selectSql;
    private String updateSql;
    private String deleteSql;
    private String insertSql;

    private boolean batch = false;
    private List<String> insertValues;

    public DefaultSqlGenerator(ProviderContext context, SqlType sqlType) {
        this.sqlType = sqlType;

        if (context.getMapperMethod().isAnnotationPresent(Options.class)) {
            options = context.getMapperMethod().getAnnotation(Options.class);
        }

        analyzeParameters(context.getMapperMethod());

        analyzeProvider(context);

        if (providerMethod == null || methodHandle == null) {
            // no provider
            analyzeMappingClass(context.getMapperMethod(), sqlType);

            this.mappingInfo = registry.mappingInfo(this.mappingClass);

            switch (sqlType) {
                case INSERT:
                    this.insertSql = buildInsert(context);
                    break;
                case SELECT:
                    this.selectSql = buildSelect(context);
                    break;
                case DELETE:
                    this.deleteSql = buildDelete(context);
                    break;
                case UPDATE:
                    this.updateSql = buildUpdate(context);
                    break;
            }
        }
    }

    /**
     * Generate sql statement for the specific mapper method
     *
     * @param context
     * @param paramObject
     * @param sqlType
     * @return
     * @throws Throwable
     */
    @Override
    public String generate(ProviderContext context, Object paramObject, SqlType sqlType) throws Throwable {
        if (providerMethod != null && methodHandle != null) {
            // if we can find the default provider method, use the default method to generate sql
            return getSqlFromProvider(paramObject);
        } else {
            // generate sql through root class
            switch (sqlType) {
                case SELECT:
                    return select(context, paramObject);
                case DELETE:
                    return delete(context, paramObject);
                case UPDATE:
                    return update(context, paramObject);
                case INSERT:
                    return insert(context, paramObject);
            }
        }

        throw new IllegalStateException("Oops,we could not generate sql from provider or by default strategy(from root class).");
    }

    /**
     * @param className
     * @return
     */
    private Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * @param mapperType
     * @return
     */
    private Class<?> getInnerSqlProviderClass(Class<?> mapperType) {
        return findClass(mapperType.getName() + "$Provider");
    }

    /**
     * @param mapperType
     * @return
     */
    private Class<?> getSqlProviderClass(Class<?> mapperType) {
        return findClass(mapperType.getName() + "Provider");
    }

    /**
     * @param mapperMethod
     * @return
     */
    private Class<?> getSqlProviderFromAnnotation(Method mapperMethod) {
        return mapperMethod.isAnnotationPresent(Provider.class) ? mapperMethod.getAnnotation(Provider.class).clazz() : null;
    }

    /**
     * Get the parameters of mapper method
     *
     * @param mapperMethod
     */
    private void analyzeParameters(Method mapperMethod) {
        this.mapperMethodParams = mapperMethod.getParameters();

        this.paramNames = new String[mapperMethodParams.length];

        int index = 0;

        for (Parameter parameter : this.mapperMethodParams) {
            if (parameter.isAnnotationPresent(Param.class)) {
                this.hasParamAnnotation = true;
                paramNames[index++] = parameter.getAnnotation(Param.class).value();
            } else {
                paramNames[index++] = parameter.getName();
            }
        }
    }

    /**
     * Get provider which provide sql statement for mapper method
     *
     * @param context
     * @return
     */
    private void analyzeProvider(ProviderContext context) {
        Class<?> mapperType = context.getMapperType();

        Method mapperMethod = context.getMapperMethod();

        providerClass = getInnerSqlProviderClass(mapperType);

        if (providerClass == null) {
            providerClass = getSqlProviderClass(mapperType);
        }

        if (providerClass == null) {
            providerClass = getSqlProviderFromAnnotation(mapperMethod);
        }

        if (providerClass != null) {
            try {
                providerMethod = providerClass.getMethod(mapperMethod.getName(), mapperMethod.getParameterTypes());

                if (providerClass.isInterface() && providerMethod.isDefault()) {
                    Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{providerClass}, (p, m, args) -> null);
                    methodHandle = ReflectionUtils.getDefaultMethodHandle(proxy, providerMethod);
                } else if (Modifier.isStatic(providerMethod.getModifiers())) {
                    methodHandle = ReflectionUtils.getStaticMethodHandle(providerClass, providerMethod);
                }
            } catch (ReflectiveOperationException e) {
                // ignore
            }
        }
    }

    /**
     * Get mapping class of the mapper
     *
     * @param mapperMethod
     * @param sqlType
     */
    private void analyzeMappingClass(Method mapperMethod, SqlType sqlType) {
        if (mapperMethod.isAnnotationPresent(Mapping.class)) {
            mappingClass = mapperMethod.getAnnotation(Mapping.class).value();
        } else if (sqlType == SqlType.SELECT) {
            // select statement, get root class from the return type of mapper method
            Type returnType = mapperMethod.getGenericReturnType();

            // return type is collection, get the actual type
            if (returnType instanceof ParameterizedType && Collection.class.isAssignableFrom(mapperMethod.getReturnType())) {
                mappingClass = ReflectionUtils.getActualType((ParameterizedType) returnType).get(0);
            } else if (returnType instanceof Class<?>) {
                // return type is not Map or Collection
                Class<?> clazz = (Class<?>) returnType;
                mappingClass = clazz.isArray() ? clazz.getComponentType() : clazz;
            }
        } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE) {
            if (mapperMethodParams.length == 1) {
                Type type = mapperMethodParams[0].getParameterizedType();

                if (type instanceof ParameterizedType && Collection.class.isAssignableFrom(mapperMethodParams[0].getType())) {
                    batch = true;
                    mappingClass = ReflectionUtils.getActualType((ParameterizedType) type).get(0);
                } else if (type instanceof Class) {
                    if (((Class) type).isArray()) {
                        batch = true;
                        mappingClass = ((Class) type).getComponentType();
                    } else {
                        mappingClass = (Class<?>) type;
                    }
                }
            }
        }


    }

    /**
     * @param paramObject
     * @param providerMethod
     * @return
     */
    private Object[] extractParams(Object paramObject, Method providerMethod) {
        if (this.mapperMethodParams.length == 0) {
            return null;
        } else if (this.mapperMethodParams.length == 1 && !this.hasParamAnnotation) {
            // if has param annotation,mybatis will put the param in a map
            return new Object[]{paramObject};
        } else if (paramObject instanceof Map) {
            Object[] params = new Object[this.mapperMethodParams.length];

            for (int i = 0, len = this.mapperMethodParams.length; i < len; i++) {
                params[i] = ((Map) paramObject).get(this.paramNames[i]);
            }
        }

        return null;
    }

    private String getSqlFromProvider(Object paramObject) throws Throwable {
        Object result;

        Object[] params = extractParams(paramObject, providerMethod);

        if (providerClass.isInterface()) {
            result = MethodHandles.spreadInvoker(methodHandle.type(), 0).invokeExact(methodHandle, params);
        } else {
            result = methodHandle.invokeExact(params);
        }

        return result == null ? null : result.toString();
    }

    private String buildSelect(ProviderContext context) {
        LOGGER.debug("Begin to generate select sql for method[{}] of class[{}].", context.getMapperMethod(), context.getMapperType());

        Select select = new Select().columns(MappingUtils.columns(this.mappingClass, true)).from(this.mappingInfo.table());

        Condition<Select> condition = condition();

        if (condition != null) {
            select.where(condition);
        }

        String sql = select.toString();

        LOGGER.debug("Generate select statement[{}] for method[{}] of class[{}], params is [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    private String select(ProviderContext context, Object paramObject) {
        return this.selectSql;
    }

    private <S extends Sql<S>> Condition<S> condition() {
        Condition<S> condition = null;

        if (sqlType == SqlType.UPDATE) {
            for (ColumnInfo columnInfo : this.mappingInfo.columnInfos()) {
                if (columnInfo.keyProperty()) {
                    if (condition == null) {
                        condition = Comparison.<S>eq(column(columnInfo.column()), bind(columnInfo.prop()));
                    } else {
                        condition.and(Comparison.<S>eq(column(columnInfo.column()), bind(columnInfo.prop())));
                    }
                }
            }
        } else {
            if (this.mapperMethodParams.length > 0) {
                condition = singleCondition(this.mapperMethodParams[0], this.paramNames[0]);

                if (this.mapperMethodParams.length > 1) {
                    for (int i = 1, len = this.mapperMethodParams.length; i < len; i++) {
                        condition.and(singleCondition(this.mapperMethodParams[i], this.paramNames[i]));
                    }
                }
            }
        }

        return condition;
    }

    private <S extends Sql<S>> Condition<S> singleCondition(Parameter parameter, String paramName) {
        Condition<S> condition;

        if (parameter.isAnnotationPresent(Criteria.class)) {
            Criteria criteria = parameter.getAnnotation(Criteria.class);

            switch (criteria.type()) {
                default:
                    condition = Comparison.<S>eq(column(criteria.column()), bind(paramName));
            }
        } else {
            condition = Comparison.<S>eq(column(paramName), bind(paramName));
        }

        return condition;
    }

    private String buildDelete(ProviderContext context) {
        LOGGER.debug("Begin to generate delete sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        Delete delete = new Delete(this.mappingInfo.table());

        Condition<Delete> condition = condition();

        if (condition != null) {
            delete.where(condition);
        }

        String sql = delete.toString();

        LOGGER.debug("Generate delete statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    private String delete(ProviderContext context, Object paramObject) {
        return this.deleteSql;
    }

    private String buildUpdate(ProviderContext context) {
        LOGGER.debug("Begin to generate update sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        Update update = new Update(this.mappingInfo.table());

        MappingUtils.consume(mappingClass, columnInfo -> !columnInfo.keyProperty() && columnInfo.update(),
                columnInfo -> update.set(columnInfo.column(), bind(columnInfo.prop())));

        Condition<Update> condition = condition();

        if (condition != null) {
            update.where(condition);
        }

        String sql = update.toString();

        LOGGER.debug("Generate update statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    /**
     * TODO nullable judge
     *
     * @param context
     * @param paramObject
     * @return
     */
    private String update(ProviderContext context, Object paramObject) {
        return this.updateSql;
    }

    /**
     * @param context
     * @return
     */
    private String buildInsert(ProviderContext context) {
        LOGGER.debug("Begin to generate insert sql for method [{}] of class [{}].", context.getMapperMethod(), context.getMapperType());

        Insert insert = new Insert(this.mappingInfo.table());

        insert.columns(MappingUtils.columns(this.mappingClass, INSERT_PREDICATE));

        if (batch) {
            String paramStart = paramNames[0] + OPEN_SQUARE_BRACKET;

            insertValues = new ArrayList<>(BATCH_INSERT_MAX_COUNT);

            for (int i = 0; i < BATCH_INSERT_MAX_COUNT; i++) {
                insertValues.add(MappingUtils.bindExpr(mappingClass, INSERT_PREDICATE, paramStart + i + OPEN_SQUARE_BRACKET, BIND));
            }
        } else {
            insert.values(MappingUtils.bind(mappingClass, INSERT_PREDICATE));
        }

        String sql = insert.toString();

        LOGGER.debug("Generate insert statement[{}] for method [{}] of class [{}]!", sql, context.getMapperMethod(), context.getMapperType());

        return sql;
    }

    /**
     * TODO nullable judge
     *
     * @param context
     * @param paramObject
     * @return
     */
    private String insert(ProviderContext context, Object paramObject) {
        if (batch) {
           Class<?> clazz = ((Map)paramObject).get("array").getClass();

            return this.insertSql + Insert.VALUES;
        } else {
            return this.insertSql;
        }
    }
}
