package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.Column;
import com.intros.mybatis.plugin.annotation.MappingClass;
import com.intros.mybatis.plugin.annotation.Provider;
import com.intros.mybatis.plugin.annotation.Table;
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

    private boolean hasParamAnnotation;

    private Parameter[] mapperMethodParams;

    private String[] paramNames;

    private Class<?> providerClass;

    private Method providerMethod;

    private MethodHandle methodHandle;

    private String table;

    private Class<?> rootClass;

    public DefaultSqlGenerator(ProviderContext context, SqlType sqlType) {
        initializeParameters(context.getMapperMethod());

        initializeProvider(context);

        if (providerMethod == null || methodHandle == null) {
            // no provider
            decideTableAndColumns(context.getMapperMethod(), sqlType);
        }

        initializeRootClass(context.getMapperMethod(), sqlType);

        if (rootClass != null) {
            table = rootClass.getAnnotation(Table.class).name();

            toColumnAttrs(rootClass);

//            if (mapperMethod.isAnnotationPresent(InsertProvider.class)) {
//                // initialize predicate list for select, update or delete
//                Arrays.stream(mapperMethodParams).forEach(parameter -> {
//                    Op op = parameter.isAnnotationPresent(Criteria.class) ? parameter.getAnnotation(Criteria.class).op() : Op.EQ;
//
//                    String paramName = paramName(parameter);
//
//                    String alias = parameter.isAnnotationPresent(Criteria.class) ? parameter.getAnnotation(Criteria.class).alias() : parameter.getName();
//                });
//            }
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
     * Initialize the parameter of mapper method
     *
     * @param mapperMethod
     */
    private void initializeParameters(Method mapperMethod) {
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
     * Initialize provider which provide sql statement for mapper method
     *
     * @param context
     * @return
     */
    private void initializeProvider(ProviderContext context) {
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

    private void decideTableAndColumns(Method mapperMethod, SqlType sqlType) {

    }

    private void initializeRootClass(Method mapperMethod, SqlType sqlType) {
        if (mapperMethod.isAnnotationPresent(MappingClass.class)) {
            if (sqlType == SqlType.SELECT) {
                // select statement, get root class from the return type of mapper method
                Type returnType = mapperMethod.getGenericReturnType();

                // return type is collection, get the actual type
                if (returnType instanceof ParameterizedType && Collection.class.isAssignableFrom(mapperMethod.getReturnType())) {
                    rootClass = ReflectionUtils.getActualType((ParameterizedType) returnType).get(0);
                } else if (returnType instanceof Class<?>) {
                    // return type is not Map or Collection
                    Class<?> clazz = (Class<?>) returnType;
                    rootClass = clazz.isArray() ? clazz.getComponentType() : clazz;
                }
            } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE) {
                if (mapperMethodParams.length == 1) {
                    rootClass = mapperMethodParams[0].getType();
                }
            }
        }
    }

    /**
     * acquire fields recursively
     *
     * @param clazz
     * @return
     */
    private List<Field> acquireFieldsRecursively(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        while (clazz != null && !Object.class.equals(clazz)) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * get column attrs from root class
     *
     * @param root
     */
    private void toColumnAttrs(Class<?> root) {
        List<Field> fields = acquireFieldsRecursively(root);

        Column column;
        String alias;

        for (Field field : fields) {
            column = field.getAnnotation(Column.class);

            if (column == null) {
                continue;
            }

            alias = Optional.ofNullable(column.alias()).orElse(field.getName());

//            columnAttrsMap.put(alias, new ColumnAttrs(alias, column.name(), column.nullable()));
        }
    }

    /**
     * @param paramObject
     * @param method
     * @return
     */
    private Object[] extractParams(Object paramObject, Method method) {
        if (this.mapperMethodParams.length == 0) {
            return null;
        } else if (this.mapperMethodParams.length == 1 && !this.hasParamAnnotation) {
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

        String sql = new SQL() {
            {
//                if (columnsMap == null || columnsMap.size() == 0) {
//                    LOGGER.error("Can't find column informations of mapper method[{}]!", context.getMapperMethod());
//                    throw new IllegalStateException("Empty column information of mapper method!");
//                }

                StringBuilder builder = new StringBuilder();

//                for (Map.Entry<String, String> entry : columnsMap.entrySet()) {
//                    builder.append(entry.getValue()).append(KW_AS_WITH_SPACE).append(entry.getKey());
//                    SELECT(builder.toString());
//                    builder.setLength(0);
//                }
//
//                FROM(table);
//
//                // TODO optimize: default param name is list for List,and array for Array parameter
//                for (Parameter parameter : mapperMethodParams) {
//                    WHERE(predicateMap.get(parameter).render(context, paramObject instanceof Map ? parseArgs((Map) paramObject, parameter) : paramObject));
//                }
            }
        }.toString();

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
