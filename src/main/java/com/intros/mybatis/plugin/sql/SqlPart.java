package com.intros.mybatis.plugin.sql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SqlPart<S extends Sql<S>> implements SqlWriter<S> {
    private static Map<Class<? extends SqlPart>, SqlPartFactory<? extends SqlPart>> factorys = new ConcurrentHashMap<>(16);

    public static void registerFactory(Class<? extends SqlPart> clazz, SqlPartFactory<? extends SqlPart> factory) {
        if (!factorys.containsKey(clazz)) {
            synchronized (factory) {
                if (!factorys.containsKey(clazz)) {
                    factorys.put(clazz, factory);
                }
            }
        }
    }

    public static <P extends SqlPart> P instance(Class<P> clazz, Object... initArgs) {
        return createInstance(clazz, initArgs);
    }

    private static <P extends SqlPart> P createInstance(Class<P> clazz, Object... initArgs) {
        SqlPartFactory<P> factory = (SqlPartFactory<P>) factorys.get(clazz);

        if (factory != null) {
            return factory.create(initArgs);
        }

        return null;
    }
}
