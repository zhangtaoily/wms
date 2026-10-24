package com.wms.common.utils;

import com.wms.common.function.Executable;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@UtilityClass
public class ConditionFunctions {


    /**
     * 切换到Chain模式，可连续调用
     */
    public ConditionFunctionChain toChain() {
        return ConditionFunctionChain.getInstance();
    }

    /**
     * 将：
     * <pre>
     * if(t!=null) {
     *     bala bala t
     * }
     * </pre>
     * 替换为：
     * <pre>
     * consumerIfNonNull(t, t -> bala bala...);
     * <pre/>
     * 需要注意的是，参数 t 如果使用方法传入时，需自行保障不会 NPE
     *
     * @param t 对象
     * @param consumer 消费 t
     */
    public <T> void consumerIfNonNull(T t, Consumer<T> consumer) {
        consumerIfNonNull(t, Function.identity(), consumer);
    }

    /**
     * 如果 t 不为空，且 fun(t) 不为空，则消费 fun(t)，替换：
     * <pre>
     *     T t = fetchT();
     *     if(t != null && t.getX() != null) {
     *         y.setX(t.getX());
     *     }
     * </pre>
     * 为：
     * <pre>
     *     T t = fetchT();
     *     consumerIfNonNull(t, t::getX, y::setX);
     * </pre>
     * 它与：{@link ConditionFunctions#consumerIfNonNull(Object, Consumer)} 不同之处在于，它不会导致NPE，
     * 而 {@link ConditionFunctions#consumerIfNonNull(Object, Consumer)} 的第一个参数在提供值时，可能会 NPE
     *
     * @param t          对象
     * @param tvFunction 对象提取函数
     * @param consumer   v消费函数
     */
    public <T, V> void consumerIfNonNull(T t, Function<T, V> tvFunction, Consumer<V> consumer) {
        if (Predicates.isNotNull(t)) {
            consumer.accept(tvFunction.apply(t));
        }
    }

    /**
     * 非空转换，空则返回默认值
     *
     * @param t                 断言对象
     * @param trueFunction      转换函数
     * @param falseDefaultValue 为空时提供的默认值
     */
    public <T, R> R funcIfNoneNull(T t, Function<T, R> trueFunction, R falseDefaultValue) {
        return funcIfNoneNull(Predicates::isNotNull, t, trueFunction, falseDefaultValue);
    }

    /**
     * 非空则转换，用于替换：
     * <pre>
     *      String registerTime = request.getRegisterTime();
     *      if(registerTime != null) {
     *          x = Date.extract(registerTime, Unit.YEAR);
     *      }
     *
     *      -->
     *
     *      String registerTime = request.getRegisterTime();
     *      x = ConditionFunctions.INSTANCE.functionIfNoneNull(registerTime, d -> Date.extract(d, Unit.YEAR))
     * </pre>
     */
    public <T, R> R funcIfNoneNull(T t, Function<T, R> trFunction) {
        return funcIfNoneNull(t, trFunction, null);
    }

    /**
     * 非空转换
     *
     * @param tPredicate        对 T 的断言
     * @param t                 对象
     * @param trueFunction      如果断言成立，执行函数
     * @param falseDefaultValue 如果不成立，返回默认值
     */
    public <T, R> R funcIfNoneNull(Predicate<T> tPredicate, T t, Function<T, R> trueFunction, R falseDefaultValue) {
        return funcIfTrue(tPredicate.test(t), t, trueFunction, x -> falseDefaultValue);
    }

    /**
     * 如果condition为真，则执行trueFunction，否则执行falseFunction
     */
    public <T, R> R funcIfTrue(boolean condition, T t, Function<T, R> trueFunction, Function<T, R> falseFunction) {
        if (condition) {
            return trueFunction.apply(t);
        }

        return falseFunction.apply(t);
    }

    /**
     * 如果condition为真，则执行trueFunction，否则执行falseFunction
     */
    public <T> T supplierIfTrue(boolean condition, Supplier<T> tSupplier) {
        if (condition) {
            return tSupplier.get();
        }

        return null;
    }

    /**
     * 当条件为真时执行后续方法
     */
    public void executeIfNoneNull(Object x, Executable executable) {
        executeIfTrue(Predicates.isNotNull(x), executable);
    }

    /**
     * 当条件为真时执行后续方法
     */
    public void executeIfTrue(boolean condition, Executable executable) {
        if (condition) {
            executable.execute();
        }
    }

    /**
     * 条件执行器
     *
     * @param condition       条件执行器
     * @param trueExecutable  true
     * @param falseExecutable false
     */
    public void executeIfTrue(boolean condition, Executable trueExecutable, Executable falseExecutable) {
        if (condition) {
            trueExecutable.execute();
            return;
        }

        falseExecutable.execute();
    }

}
