package com.wms.service.common.meta;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Lambda表达式字段名解析工具类
 * <p>
 * 提供统一的字段名解析功能，支持通过lambda表达式获取实体类的数据库列名。
 * 该工具类被TableNodeBuilder、AbstractDynamicsFormService等类复用。
 * </p>
 *
 * @author wms
 */
public class LambdaFieldUtils {

    /**
     * 私有构造函数，防止实例化工具类
     */
    private LambdaFieldUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 解析实体类字段名（支持lambda表达式）
     * <p>
     * 通过解析SFunction的SerializedLambda，获取字段名并映射到数据库列名。
     * 利用 MyBatis-Plus 的 TableInfoHelper.getTableInfo 方法递归获取父类字段信息。
     * </p>
     *
     * @param fn lambda表达式函数，如：Entity::getFieldName
     * @param <T> 实体类型
     * @return 数据库列名
     * @throws RuntimeException 当字段解析失败时抛出异常
     */
    public static <T> String getColumnName(SFunction<T, ?> fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            Object serializedLambda = method.invoke(fn);

            SerializedLambda lambda = (SerializedLambda) serializedLambda;
            String methodName = lambda.getImplMethodName();
            String implClass = lambda.getImplClass();

            String fieldName = methodName.startsWith("get")
                ? methodName.substring(3)
                : methodName;
            String fieldNameFinal = Introspector.decapitalize(fieldName);

            Class<?> clazz = Class.forName(implClass.replace('/', '.'));

            // 尝试从 TableInfoHelper 获取 TableInfo
            TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);

            // 如果当前类没有 TableInfo，尝试递归查找有 @TableName 注解的父类
            if (tableInfo == null) {
                Class<?> currentClass = clazz;
                while (currentClass != null && !currentClass.equals(Object.class)) {
                    try {
                        tableInfo = TableInfoHelper.getTableInfo(currentClass);
                        if (tableInfo != null) {
                            break;
                        }
                    } catch (Exception e) {
                        // 忽略异常，继续查找父类
                    }
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (tableInfo == null) {
                // 如果仍然没有找到 TableInfo，尝试通过反射直接获取字段信息
                try {
                    Field field = findField(clazz, fieldNameFinal);
                    if (field != null) {
                        com.baomidou.mybatisplus.annotation.TableField tableField = field.getAnnotation(com.baomidou.mybatisplus.annotation.TableField.class);
                        if (tableField != null && !tableField.exist()) {
                            // 字段不存在于数据库中
                            throw new RuntimeException("字段不存在于数据库中：" + fieldName);
                        }
                        // 如果有 @TableField 注解且设置了 value，则使用 value 作为列名
                        if (tableField != null && tableField.value() != null && !tableField.value().isEmpty()) {
                            return tableField.value();
                        }
                        // 否则使用字段名作为列名
                        return field.getName();
                    }
                } catch (Exception e) {
                    // 忽略异常，继续使用 null
                }

                throw new RuntimeException("无法获取实体类信息，请检查类是否被 MyBatis-Plus 扫描：" + clazz.getName());
            }

            // 直接从 TableInfo 中查找字段（MyBatis-Plus 已递归处理父类）
            TableFieldInfo fieldInfo = tableInfo.getFieldList().stream()
                .filter(f -> f.getProperty().equals(fieldNameFinal))
                .findFirst()
                .orElse(null);

            if (fieldInfo != null) {
                return fieldInfo.getColumn();
            }

            throw new RuntimeException("请检查@TableField配置：" + fieldName);
        } catch (Exception e) {
            throw new RuntimeException("字段解析失败，请检查方法引用格式", e);
        }
    }

    /**
     * 递归查找类中的字段
     *
     * @param clazz 类
     * @param fieldName 字段名
     * @return 字段，如果找不到则返回 null
     */
    private static java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field;
            }
        } catch (Exception e) {
            // 忽略异常，继续查找父类
        }

        // 查找父类
        Class<?> parentClass = clazz.getSuperclass();
        if (parentClass != null && !parentClass.equals(Object.class)) {
            return findField(parentClass, fieldName);
        }

        return null;
    }

    /**
     * 解析实体类字段名（支持lambda表达式）
     * <p>
     * 与getColumnName(SFunction)功能相同，但接受Function接口作为参数。
     * 用于兼容不使用SFunction的场景。
     * </p>
     *
     * @param fn lambda表达式函数，如：Entity::getFieldName
     * @param <T> 实体类型
     * @return 数据库列名
     * @throws RuntimeException 当字段解析失败时抛出异常
     */
    public static <T> String getColumnName(Function<T, ?> fn) {
        return getColumnName((SFunction<T, ?>) fn);
    }

    /**
     * 获取实体的属性名（支持lambda表达式）
     * <p>
     * 通过解析SFunction的SerializedLambda，获取字段名（属性名）。
     * 此方法返回的是实体的属性名，而不是数据库列名。
     * </p>
     *
     * @param fn lambda表达式函数，如：Entity::getFieldName
     * @param <T> 实体类型
     * @return 实体属性名
     * @throws RuntimeException 当字段解析失败时抛出异常
     */
    public static <T> String getFieldName(SFunction<T, ?> fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            Object serializedLambda = method.invoke(fn);

            SerializedLambda lambda = (SerializedLambda) serializedLambda;
            String methodName = lambda.getImplMethodName();

            String fieldName = methodName.startsWith("get")
                ? methodName.substring(3)
                : methodName;
            return Introspector.decapitalize(fieldName);
        } catch (Exception e) {
            throw new RuntimeException("字段解析失败，请检查方法引用格式", e);
        }
    }

    /**
     * 获取实体的属性名（支持lambda表达式）
     * <p>
     * 与getFieldName(SFunction)功能相同，但接受Function接口作为参数。
     * 用于兼容不使用SFunction的场景。
     * </p>
     *
     * @param fn lambda表达式函数，如：Entity::getFieldName
     * @param <T> 实体类型
     * @return 实体属性名
     * @throws RuntimeException 当字段解析失败时抛出异常
     */
    public static <T> String getFieldName(Function<T, ?> fn) {
        return getFieldName((SFunction<T, ?>) fn);
    }
}
