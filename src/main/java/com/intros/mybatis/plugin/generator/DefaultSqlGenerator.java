package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.MappingInfo;
import com.intros.mybatis.plugin.MappingInfoRegistry;
import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.Mapping;
import com.intros.mybatis.plugin.annotation.Provider;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.Table;
import com.intros.mybatis.plugin.sql.constants.Join;
import com.intros.mybatis.plugin.sql.expression.Expression;
import com.intros.mybatis.plugin.utils.ReflectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;

import static com.intros.mybatis.plugin.sql.Table.table;
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
    private static final char TABLE_ALIAS_BEGIN = 'a';
    private static MappingInfoRegistry registry = MappingInfoRegistry.getInstance();
    private boolean hasParamAnnotation;
    private Parameter[] mapperMethodParams;
    private String[] paramNames;
    private Class<?> providerClass;
    private Method providerMethod;
    private MethodHandle methodHandle;
    private Class<?> mappingClass;
    private LinkedList<Class<?>> mappingClasses = new LinkedList<>();
    private LinkedList<MappingInfo> mappingInfos = new LinkedList<>();

    public DefaultSqlGenerator(ProviderContext context, SqlType sqlType) {
        analyzeParameters(context.getMapperMethod());

        analyzeProvider(context);

        if (providerMethod == null || methodHandle == null) {
            // no provider
            analyzeMapping(context.getMapperMethod(), sqlType);
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
    private void analyzeMapping(Method mapperMethod, SqlType sqlType) {
        if (sqlType == SqlType.SELECT) {
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
                mappingClass = mapperMethodParams[0].getType();
            }
        }

        if (mapperMethod.isAnnotationPresent(Mapping.class)) {
            mappingClass = mapperMethod.getAnnotation(Mapping.class).value();
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

    private String select(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate select sql for method[{}] of class[{}], params is [{}].", context.getMapperMethod(), context.getMapperType(), paramObject);

        List<Expression<Select>> expressions = new ArrayList<>(8);

        List<Table> tables = new ArrayList<>();

        char alias = TABLE_ALIAS_BEGIN;

        for (MappingInfo mappingInfo : this.mappingInfos) {
            tables.add(table(mappingInfo.table()).as(String.valueOf(alias)));

            for (MappingInfo.ColumnInfo columnInfo : mappingInfo.columnInfos()) {
                expressions.add(column(columnInfo.column()).as(String.valueOf(columnInfo.alias())));
            }

            alias++;
        }

        Select select = new Select();

        for (Expression expression : expressions) {
            select.columns(expression);
        }

        select.from(tables.get(0));

        for (int i = 1; i < tables.size(); i++) {
            select.join(tables.get(i), Join.INNER);
        }

//        if (mapperMethodParams.length > 0) {
//            select.where();
//        }

        String sql = select.toString();

        LOGGER.debug("Generate select statement[{}] for method[{}] of class[{}], params is [{}].!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

        return sql;
    }

    private String delete(ProviderContext context, Object paramObject) {
        LOGGER.debug("Begin to generate delete sql for method [{}] of class [{}], params is [{}].", context.getMapperMethod(), context.getMapperType(), paramObject);

        String sql = new SQL() {
            {
//                DELETE_FROM(table);

                // TODO optimize: default param name is list for List,and array for Array parameter
//                for (Parameter parameter : mapperMethodParams) {
//                    WHERE(predicateMap.get(parameter).render(context, paramObject instanceof Map ? parseArgs((Map) paramObject, parameter) : paramObject));
//                }
            }
        }.toString();

        LOGGER.debug("Generate delete statement[{}] for method [{}] of class [{}], params is [{}].!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

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
        LOGGER.debug("Begin to generate update sql for method [{}] of class [{}], params is [{}].", context.getMapperMethod(), context.getMapperType(), paramObject);

        String sql = new SQL() {
            {
//                UPDATE(table);

                StringBuilder builder = new StringBuilder();

//                for (Map.Entry<String, String> entry : columnsMap.entrySet()) {
//                    builder.append(entry.getValue()).append(KW_EQU_WITH_SPACE).append(PARAM_NAME_PREFIX).append(entry.getKey()).append(PARAM_NAME_SUFFIX);
//                    SET(builder.toString());
//                    builder.setLength(0);
//                }
//
//                // TODO optimize: default param name is list for List,and array for Array parameter
//                for (Parameter parameter : mapperMethodParams) {
//                    WHERE(predicateMap.get(parameter).render(context, paramObject instanceof Map ? parseArgs((Map) paramObject, parameter) : paramObject));
//                }
            }
        }.toString();

        LOGGER.debug("Generate update statement[{}] for method [{}] of class [{}], params is [{}].!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

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
        LOGGER.debug("Begin to generate insert sql for method [{}] of class [{}], params is [{}].", context.getMapperMethod(), context.getMapperType(), paramObject);

        String sql = new SQL() {
            {
//                INSERT_INTO(table);

                StringBuilder builder = new StringBuilder();

//                for (Map.Entry<String, String> entry : columnsMap.entrySet()) {
//                    builder.append(PARAM_NAME_PREFIX).append(entry.getKey()).append(PARAM_NAME_SUFFIX);
//                    INTO_COLUMNS(entry.getValue()).INTO_VALUES(builder.toString());
//                    builder.setLength(0);
//                }
            }
        }.toString();

        LOGGER.debug("Generate insert statement[{}] for method [{}] of class [{}], params is [{}].!", sql, context.getMapperMethod(), context.getMapperType(), paramObject);

        return sql;
    }
}
