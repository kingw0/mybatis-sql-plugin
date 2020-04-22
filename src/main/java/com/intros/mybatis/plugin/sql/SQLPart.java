package com.intros.mybatis.plugin.sql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SQLPart<S extends SQL<S>> implements SQLWriter<S> {
    private static Map<Class<? extends SQLPart>, SQLPartFactory<? extends SQLPart>> factorys = new ConcurrentHashMap<>(16);

    public static void registerFactory(Class<? extends SQLPart> clazz, SQLPartFactory<? extends SQLPart> factory) {
        if (!factorys.containsKey(clazz)) {
            synchronized (factory) {
                if (!factorys.containsKey(clazz)) {
                    factorys.put(clazz, factory);
                }
            }
        }
    }

    public static <P extends SQLPart> P instance(Class<P> clazz, Object... initArgs) {
        // TODO,use object pool to reduce object creation cost
        return createInstance(clazz, initArgs);
    }

    private static <P extends SQLPart> P createInstance(Class<P> clazz, Object... initArgs) {
        SQLPartFactory<P> factory = (SQLPartFactory<P>) factorys.get(clazz);

        if (factory != null) {
            return factory.create(initArgs);
        }

        return null;
    }
}
