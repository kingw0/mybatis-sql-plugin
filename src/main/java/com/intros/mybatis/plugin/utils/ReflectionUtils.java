package com.intros.mybatis.plugin.utils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author teddy
 * @since 2019/5/20
 */
public class ReflectionUtils {

    /**
     * @param clazz
     * @param name
     * @param parameterTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name, parameterTypes);
    }

    /**
     * Get actual type of Parameterized type
     *
     * @param parameterizedType
     * @return
     */
    public static List<Class<?>> getActualType(ParameterizedType parameterizedType) {
        Type[] types = parameterizedType.getActualTypeArguments();
        return Arrays.stream(types).map(type -> (Class<?>) type).collect(Collectors.toList());
    }

    /**
     * Get annotation specified by annotationClass of a AnnotatedElement.
     * if the AnnotatedElement has no the annotation, return null.
     *
     * @param annotatedElement
     * @param annotationClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationClass) {
        if (annotatedElement.isAnnotationPresent(annotationClass)) {
            return annotatedElement.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * method handle of interface default method
     *
     * @param proxy
     * @param method
     * @return
     * @throws ReflectiveOperationException
     */
    public static MethodHandle getDefaultMethodHandle(Object proxy, Method method) throws ReflectiveOperationException {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);

        Class<?> declaringClass = method.getDeclaringClass();
        int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;

        return constructor.newInstance(declaringClass, allModes).unreflectSpecial(method, declaringClass).bindTo(proxy).asType(MethodType.genericMethodType(method.getParameterCount(), false));
    }

    /**
     * Method handle of static method
     *
     * @param clazz
     * @param method
     * @return
     * @throws ReflectiveOperationException
     */
    public static MethodHandle getStaticMethodHandle(Class<?> clazz, Method method) throws ReflectiveOperationException {
        MethodType methodType = MethodType.methodType(clazz, method.getParameterTypes());
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        return lookup.findStatic(clazz, method.getName(), methodType);
    }

    /**
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public static Object invokeDefaultMethod(Object proxy, Method method, Object... args)
            throws Throwable {
        return getDefaultMethodHandle(proxy, method).invoke(args);
    }

    public static List<Field> acquireFields(Class<?> clazz, Predicate<Field> predicate) {
        List<Field> res = new ArrayList<>(16);

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (predicate == null || predicate.test(field)) {
                res.add(field);
            }
        }

        return res;
    }

    /**
     * @param clazz
     * @param predicate
     * @return
     */
    public static List<Field> acquireFieldsRecursively(Class<?> clazz, Predicate<Field> predicate) {
        List<Field> res = new ArrayList<>(16);

        Field[] fields;

        while (clazz != null && !Object.class.equals(clazz)) {
            fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (predicate == null || predicate.test(field)) {
                    res.add(field);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return res;
    }

    /**
     * acquire fields recursively
     *
     * @param clazz
     * @return
     */
    public static List<Field> acquireFieldsRecursively(Class<?> clazz) {
        return acquireFieldsRecursively(clazz, null);
    }
}
