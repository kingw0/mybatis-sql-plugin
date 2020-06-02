package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.annotation.Mapping;
import com.intros.mybatis.plugin.annotation.Provider;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Comparison;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.expression.Bind;
import com.intros.mybatis.plugin.utils.ReflectionUtils;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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

    private static MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    protected Options options;
    // mapping info
    protected Class<?> mappingClass;
    protected MappingInfo mappingInfo;
    protected boolean multiQuery = false;
    protected boolean hasParamAnnotation;
    protected Parameter[] mapperMethodParams;
    protected String[] paramNames;
    protected boolean hasProvider = true;
    private Class<?> providerClass;
    private Method providerMethod;
    private MethodHandle methodHandle;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public DefaultSqlGenerator(ProviderContext context, SqlType sqlType) {
        if (context.getMapperMethod().isAnnotationPresent(Options.class)) {
            options = context.getMapperMethod().getAnnotation(Options.class);
        }

        analyzeParameters(context.getMapperMethod());

        analyzeProvider(context);

        if (providerMethod == null || methodHandle == null) {
            hasProvider = false;

            // no provider
            analyzeMappingClass(context.getMapperMethod(), sqlType);

            this.mappingInfo = registry.mappingInfo(this.mappingClass);
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
        } else if (mappingClass != null) {
            // generate sql through mapping class
            return sql(context, paramObject);
        }

        throw new IllegalStateException("Oops,we could not generate sql from provider or by default strategy(from root class).");
    }

    /**
     * override by sub class
     *
     * @param context
     * @param paramObject
     * @return
     */
    protected String sql(ProviderContext context, Object paramObject) {
        return null;
    }

    /**
     * @param paramObject
     * @param paramName
     * @return
     */
    protected Object extractParam(Object paramObject, String paramName) {
        if (this.mapperMethodParams.length == 0) {
            return null;
        } else if (paramObject instanceof Map) {
            return ((Map) paramObject).get(paramName);
        } else if (this.mapperMethodParams.length == 1 && !this.hasParamAnnotation) {
            // if has param annotation,mybatis will put the param in a map
            return paramObject;
        }

        return null;
    }

    /**
     * get param size if param is collection or array
     *
     * @param paramObj
     * @param paramType
     * @return
     */
    protected int paramSize(Object paramObj, Class<?> paramType) {
        int size = -1;

        if (Collection.class.isAssignableFrom(paramType)) {
            size = ((Collection) paramObj).size();
        } else if (paramType.isArray()) {
            size = ((Object[]) paramObj).length;
        }

        return size;
    }

    /**
     * Get condition from the mapping class's key property
     *
     * @param paramName
     * @param <S>
     * @return
     */
    protected <S extends Sql<S>> Condition<S> mappingCondition(String paramName) {
        Condition<S> condition = null;

        // construct condition from mapping class
        for (ColumnInfo columnInfo : this.mappingInfo.columnInfos()) {
            if (options != null && columnInfo.prop().equals(options.keyProperty())) {
                Condition<S> cond = Comparison.<S>eq(column(columnInfo.column()), bind(paramName, Arrays.asList(columnInfo.prop())));
                condition = condition == null ? cond : condition.and(cond);
            }
        }

        return condition;
    }

    protected <S extends Sql<S>> Condition<S> prepareCondition(Object paramObject) {
        Condition<S> condition = null;

        for (int i = 0, len = this.paramNames.length; i < len; i++) {
            Parameter parameter = this.mapperMethodParams[i];

            if (parameter.isAnnotationPresent(Criteria.class)) {
                Criteria criteria = parameter.getAnnotation(Criteria.class);
                Condition<S> cond = condition(criteria.type(), criteria.column(), paramNames[i], extractParam(paramObject, paramNames[i]));
                condition = condition == null ? cond : condition.and(cond);
            }
        }

        return condition;
    }

    private <S extends Sql<S>> Condition<S> condition(String type, String column, String paramName, Object paramValue) {
        switch (type) {
            case "in":
                return column(column).in(Bind.bind(paramName, paramSize(paramValue, paramValue.getClass())));
            default:
                return Comparison.<S>eq(column(column), bind(paramName));
        }
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

        if (index == 1) {
            // parameter length is one
            Class<?> type = this.mapperMethodParams[0].getType();

            if (Collection.class.isAssignableFrom(type)) {
                paramNames[0] = "collection";
            } else if (type.isArray()) {
                paramNames[0] = "array";
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
                    multiQuery = true;
                    mappingClass = ReflectionUtils.getActualType((ParameterizedType) type).get(0);
                } else if (type instanceof Class) {
                    if (((Class) type).isArray()) {
                        multiQuery = true;
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

    /**
     * @param criteria
     * @param paramName
     * @param <S>
     * @return
     */
    private <S extends Sql<S>> Condition<S> condition(Criteria criteria, String paramName, int size) {
        Condition<S> condition;

        switch (criteria.type()) {
            case "in":
                condition = column(criteria.column()).in(Bind.bind(paramName, size));
                break;
            default:
                condition = Comparison.<S>eq(column(criteria.column()), bind(paramName));
        }

        return condition;
    }
}
