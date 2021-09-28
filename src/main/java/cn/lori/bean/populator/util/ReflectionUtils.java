package cn.lori.bean.populator.util;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtils {

    private final static Logger logger = LogManager.getLogger(ReflectionUtils.class);

    public static <T> Optional<T> newInstance(Class<T> clazz) {
        try {
            Optional<Constructor<?>> found = Arrays.stream(clazz.getDeclaredConstructors()).filter(ctor -> ctor.getGenericParameterTypes().length == 0).findFirst();
            if (found.isPresent())
                return Optional.ofNullable((T) found.get().newInstance());
            else
                throw new RuntimeException("NoArgConstructor is missing of:" + clazz.getSimpleName());
        } catch (Exception e) {
            logger.error("Fail to newInstance with NoArgConstructor " + e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static String getFieldValueAsString(final Object object, final String fieldName) {
        try {
            Object v = getFieldValue(object, fieldName);
            if (v == null) return "";
            return String.valueOf(v);
        } catch (Exception e) {
            logger.warn("No string value available of field: " + object.getClass().getSimpleName() + "." + fieldName);
            return "";
        }
    }

    public static Object getFieldValue(final Object object, final String fieldName) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

        makeAccessible(field);

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            logger.error("impossbile exceptions " + e.getMessage(), e);
        }
        return result;
    }

    public static void setFieldValue(final Object object, final String fieldName, final Object value) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            logger.error("impossbile exceptions " + e.getMessage(), e);
        }
    }

    public static void setFieldValueWithCast(final Object object, final String fieldName, final String value) {
        switch (getDeclaredField(object, fieldName).getType().getSimpleName()) {
            case "boolean":
            case "Boolean":
                setFieldValue(object, fieldName, Boolean.valueOf(value));
                break;
            case "int":
            case "Integer":
                setFieldValue(object, fieldName, Integer.valueOf(value));
                break;
            case "long":
            case "Long":
                setFieldValue(object, fieldName, Long.valueOf(value));
                break;
            case "Map":
                setFieldValueForMap(object, fieldName, value);
                break;
            case "List":
                setFieldValueForList(object, fieldName, value);
                break;
            default:
                setFieldValue(object, fieldName, value);
        }
    }

    private static void setFieldValueForList(Object object, String fieldName, String value) {
        Field field = getDeclaredField(object, fieldName);
        Type[] types = getFieldActualTypeArguments(field);
        if (types == null || types.length < 1) {
            setFieldValue(object, fieldName, JacksonUtils.jsonToObject(value, List.class));
        } else {
            setFieldValue(object, fieldName, JacksonUtils.jsonToList(value, getClassBy(types[0])));
        }
    }

    private static Class getClassBy(Type type) {
        try {
            return Class.forName(type.getTypeName());
        } catch (Exception e) {
            logger.warn("Fail to getClassBy ", e);
            return Object.class;
        }
    }

    private static void setFieldValueForMap(Object object, String fieldName, String value) {
        Field field = getDeclaredField(object, fieldName);
        Type[] types = getFieldActualTypeArguments(field);
        if (types == null || types.length < 2) {
            setFieldValue(object, fieldName, JacksonUtils.jsonToObject(value, Map.class));
        } else {
            setFieldValue(object, fieldName, JacksonUtils.jsonToMap(value, getClassBy(types[0]), getClassBy(types[1])));
        }
    }

    public static Object invokeMethod(final Object object, final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] parameters) throws InvocationTargetException {
        Method method = getDeclaredMethod(object, methodName, parameterTypes);
        if (method == null)
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");

        method.setAccessible(true);

        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException e) {
            logger.error("impossible exceptions " + e.getMessage(), e);
        }

        return null;
    }

    public static Field getDeclaredField(final Object object, final String fieldName) {
        Assert.notNull(object, "object cant be null");
        Assert.hasText(fieldName, "fieldName");
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    public static <A extends Annotation> Map<String, A> getClassName2DeclaredAnnotation(final Object object, Class<A> annotation) {
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz
                .getSuperclass()) {

            A found = clazz.getDeclaredAnnotation(annotation);

            if (found != null)
                return Collections.singletonMap(clazz.getSimpleName(), found);
        }
        return Maps.newLinkedHashMap();
    }

    public static Boolean hasAnnotation(Field field, Class annotation) {
        return field.getAnnotation(annotation) != null;
    }

    public static <T extends Annotation> T getAnnotation(Field field, Class<? extends Annotation> annotation) {
        return (T) field.getAnnotation(annotation);
    }

    /**
     * 获取 field的 泛型约束
     */
    public static Type[] getFieldActualTypeArguments(Field fieldHasActualTypeArguments) {
        Type type = fieldHasActualTypeArguments.getGenericType();
        if (ParameterizedType.class.isInstance(type)) {
            ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
            return parameterizedType.getActualTypeArguments();
        }
        return new Type[0];
    }

    protected static void makeAccessible(final Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    protected static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {
        Assert.notNull(object, "object cant be null");

        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    @SuppressWarnings({"rawtypes"})
    public static Class getSuperClassGenricType(final Class clazz, final int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }
        return (Class) params[index];
    }

    public static void convertToUncheckedException(Exception e) throws IllegalArgumentException {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException)
            throw new IllegalArgumentException("Refelction Exception.", e);
        else
            throw new IllegalArgumentException(e);
    }
}