package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.annotation.Mapping;
import com.intros.mybatis.plugin.annotation.Provider;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.condition.generator.ConditionGenerator;
import com.intros.mybatis.plugin.utils.ReflectionUtils;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;

import static com.intros.mybatis.plugin.sql.expression.Binder.bindIndexProp;
import static com.intros.mybatis.plugin.sql.expression.Binder.bindProp;
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
    public static final String GENERIC_NAME_PREFIX = "param";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSqlGenerator.class);
    private static MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    protected Options options;
    // mapping info
    protected Class<?> mappingClass;
    protected MappingInfo mappingInfo;
    protected Optional<ColumnInfo> keyColumn;
    protected boolean multiQuery = false;
    protected boolean hasParamAnnotation;
    protected Parameter[] mapperMethodParams;
    protected Map<Parameter, ConditionGenerator> conditions = new HashMap<>();
    protected Map<Parameter, Criteria> criteria = new HashMap<>();
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
        analyzeParameters(context.getMapperMethod());

        analyzeProvider(context);

        if (providerMethod == null || methodHandle == null) {
            hasProvider = false;

            if (context.getMapperMethod().isAnnotationPresent(Options.class)) {
                options = context.getMapperMethod().getAnnotation(Options.class);
            }

            // no provider
            analyzeMappingClass(context.getMapperMethod(), sqlType);

            this.mappingInfo = registry.mappingInfo(this.mappingClass);

            if (options != null) {
                this.keyColumn = this.mappingInfo.columnInfos().stream().filter(columnInfo -> columnInfo.prop().equals(options.keyProperty())).findFirst();
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
    protected String sql(ProviderContext context, Object paramObject) throws Exception {
        return null;
    }

    /**
     * Extract param value from mybatis wrapped parameter
     *
     * <p>
     * {@link  org.apache.ibatis.reflection.ParamNameResolver#getNamedParams}
     * </p>
     *
     * @param param
     * @param paramName
     * @return
     */
    protected Object getArgByParamName(Object param, String paramName) {
        if (this.mapperMethodParams.length == 0) {
            return null;
        } else if (param instanceof Map) {
            // if has param annotation,mybatis will put the param in a map
            return ((Map) param).get(paramName);
        } else if (this.mapperMethodParams.length == 1 && !this.hasParamAnnotation) {
            return param;
        }

        return null;
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
                Condition<S> cond = column(columnInfo.column()).eq(bindProp(paramName, columnInfo.prop()));
                condition = condition == null ? cond : condition.and(cond);
            }
        }

        return condition;
    }

    /**
     * Construct condition from key property of param object
     *
     * @param paramName
     * @param <S>
     * @return
     */
    protected <S extends Sql<S>> Condition<S> queryCondByKeyProperty(String paramName) {
        if (!this.keyColumn.isPresent()) {
            throw new IllegalStateException("Mapper method has no options with key property!");
        }

        ColumnInfo bindColumn = keyColumn.get();

        return column(bindColumn.column()).eq(bindProp(paramName, bindColumn.prop()));
    }

    protected <S extends Sql<S>> Condition<S> queryCondByKeyProperty(String paramName, int index) {
        if (!this.keyColumn.isPresent()) {
            throw new IllegalStateException("Mapper method has no options with key property!");
        }

        ColumnInfo bindColumn = keyColumn.get();

        return column(bindColumn.column()).eq(bindIndexProp(paramName, index, bindColumn.prop()));
    }


    /**
     * Construct condition from param's criteria annotation
     *
     * @param paramObject
     * @param <S>
     * @return
     */
    protected <S extends Sql<S>> Condition<S> queryCondByCriteria(Object paramObject) {
        Condition<S> condition = null;

        for (int i = 0, len = this.paramNames.length; i < len; i++) {
            Parameter parameter = this.mapperMethodParams[i];

            ConditionGenerator conditionGenerator = conditions.get(parameter);

            if (conditionGenerator != null) {
                Condition<S> newCondition = conditionGenerator.doBuild(criteria.get(parameter), paramNames[i], getArgByParamName(paramObject, paramNames[i]));

                if (newCondition == null) {
                    continue;
                }

                condition = condition == null ? newCondition : condition.and(newCondition);
            }
        }

        return condition;
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
     * <p>
     *
     * </p>
     *
     * @param mapperMethod
     */
    private void analyzeParameters(Method mapperMethod) {
        this.mapperMethodParams = mapperMethod.getParameters();

        this.paramNames = new String[mapperMethodParams.length];

        int index = 0;

        for (Parameter parameter : this.mapperMethodParams) {
            if (!hasParamAnnotation && parameter.isAnnotationPresent(Param.class)) {
                this.hasParamAnnotation = true;
            }

            paramNames[index++] = paramName(parameter, index);

            // prepare query condition
            if (parameter.isAnnotationPresent(Criteria.class)) {
                Criteria criterion = parameter.getAnnotation(Criteria.class);

                try {
                    conditions.put(parameter, criterion.condition().newInstance());
                    criteria.put(parameter, criterion);
                } catch (ReflectiveOperationException e) {
                    int pos = index - 1;
                    LOGGER.warn("Failed to create criteria of parameter[{}] at pos[{}]!", paramNames[pos], pos, e);
                }
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
//                LOGGER.warn("Failed to get provider method!", e);
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
     * Get parameter's name by mybatis rule
     *
     * <p>
     * Mybatis will wrap parameter by parameter's name before execute statement.
     * You can find how mybatis get parameter in {@link org.apache.ibatis.reflection.ParamNameResolver} and {@link org.apache.ibatis.session.defaults.DefaultSqlSession#wrapCollection(Object}
     * </p>
     *
     * @param parameter
     * @return
     */
    private String paramName(Parameter parameter, int index) {
        String paramName;

        if (parameter.isAnnotationPresent(Param.class)) {
            paramName = parameter.getAnnotation(Param.class).value();
        } else {
            paramName = parameter.getName();
        }

        if (this.mapperMethodParams.length == 1 && !this.hasParamAnnotation) {
            Class<?> type = this.mapperMethodParams[0].getType();

            if (List.class.isAssignableFrom(type)) {
                paramName = "list";
            } else if (Collection.class.isAssignableFrom(type)) {
                paramName = "collection";
            } else if (type.isArray()) {
                paramName = "array";
            }
        }

        if (paramName == null) {
            return GENERIC_NAME_PREFIX + index;
        }

        return paramName;
    }

    /**
     * Convert sql param to mapper method args
     *
     * <p>
     * Mybatis will use {@link org.apache.ibatis.reflection.ParamNameResolver#getNamedParams(Object[])} to convert mapper method to sql command param.
     * We use this method to convert sql command param back to args
     * </p>
     *
     * @param param
     * @return
     */
    private Object[] convertSqlParamToArgs(Object param) {
        int len = this.mapperMethodParams.length;

        Object[] args = len == 0 || param == null ? null : new Object[len];

        if (param instanceof Map) {
            for (int index = 0; index < len; index++) {
                args[index] = ((Map) param).get(this.paramNames[index]);
            }
        } else if (len == 1 && !this.hasParamAnnotation) {
            args[0] = param;
        }

        return args;
    }

    private String getSqlFromProvider(Object paramObject) throws Throwable {
        Object result;

        Object[] args = convertSqlParamToArgs(paramObject);

        if (providerClass.isInterface()) {
            result = MethodHandles.spreadInvoker(methodHandle.type(), 0).invokeExact(methodHandle, args);
        } else {
            result = methodHandle.invokeWithArguments(args);
        }

        return result == null ? null : result.toString();
    }
}
