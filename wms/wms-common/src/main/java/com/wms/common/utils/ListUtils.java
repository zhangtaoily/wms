package com.wms.common.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.wms.common.core.domain.R;
import com.wms.common.exception.UtilException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.utility.Iterate;

import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

@UtilityClass
public class ListUtils {

    public static boolean isEmpty(Iterable<?> target) {
        return target == null || adaptIterable(target).isEmpty();
    }

    public static boolean allEmpty(Iterable<?>... lists) {
        return adaptItems(lists).allSatisfy(x -> adaptIterable(x).isEmpty());
    }

    public static boolean isNotEmpty(Iterable<?> target) {
        return !isEmpty(target);
    }

    /**
     * <pre>
     *      list分割工具类
     * </pre>
     *
     * @param source 源数据list
     * @param size   list分割块的大小
     */
    public static <T> List<List<T>> splitSize(List<T> source, int size) {
        if (size <= 0) {
            throw new UtilException("列表分割参数异常");
        }
        if (null == source || source.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<List<T>> partitions = new ArrayList<>();
        if (source.size() > size) {
            int remainder = source.size() % size;
            int times = source.size() / size;
            int offset = 0;
            for (int i = 0; i < times; i++) {
                partitions.add(source.subList(offset, offset += size));
            }
            if (remainder > 0) {
                partitions.add(source.subList(offset, offset + remainder));
            }
        } else {
            partitions.add(source);
        }
        return partitions;
    }

    /**
     * 分割并处理
     */
    public static <T> void splitSizeConsumer(List<T> list, int size, Consumer<List<T>> consumer) {
        splitSize(list, size).forEach(consumer);
    }

    /**
     * 等分集合，多余的平均分配到其他
     *
     * @param source 集合
     * @param limit  需要等分几份
     */
    public static <T> List<List<T>> splitAvg(List<T> source, int limit) {
        //调用hutool
        return ListUtil.splitAvg(source, limit);
    }

    /**
     * 分割并处理
     */
    public static <T> void splitAvgConsumer(List<T> list, int size, Consumer<List<T>> consumer) {
        splitAvg(list, size).forEach(consumer::accept);
    }

    /**
     * 从列表中查找一个对象，若符合条件者有多个，返回第一个
     *
     * @param list      集合
     * @param predicate 条件断言
     * @param <T>       集合中的类型
     * @return 符合条件的一个选项
     */
    public static <T> Optional<T> find(List<T> list, Predicate<T> predicate) {
        MutableList<T> select = adaptIterable(list).select(predicate::test);
        if (ObjectUtil.nonNullOrEmpty(select)) {
            return Optional.of(select.get(0));
        }
        return Optional.empty();
    }

    /**
     * 校验列表是否存在符合条件的对象
     *
     * @param list      - 集合
     * @param predicate - 条件断言
     * @return 是否存在符合条件的对象
     */
    public static <T> boolean anyMatch(List<T> list, Predicate<T> predicate) {
        return adaptIterable(list).anySatisfy(predicate::test);
    }

    /**
     * 校验列表是否全部符合条件的对象
     *
     * @param list      - 集合
     * @param predicate - 条件断言
     * @return 是否存在符合条件的对象
     */
    public static <T> boolean allMatch(List<T> list, Predicate<T> predicate) {
        return adaptIterable(list).allSatisfy(predicate::test);
    }

    /**
     * 从列表中查找符合条件列表，返回
     *
     * @param list       原始列表
     * @param tPredicate 条件断言
     * @param <T>        类型
     * @return 满足条件的列表
     */
    public static <T> MutableList<T> select(List<T> list, Predicate<T> tPredicate) {
        return adaptIterable(list).select(tPredicate::test);
    }

    /**
     * 从列表中查找符合条件列表，返回
     *
     * @param array      原始列表
     * @param tPredicate 条件断言
     * @param <T>        类型
     * @return 满足条件的列表
     */
    public static <T> MutableList<T> select(T[] array, Predicate<T> tPredicate) {
        return adaptArray(array).select(tPredicate::test);
    }

    /**
     * 比较两个集合是否相等
     *
     * @param expected 集合1
     * @param actual   集合2
     * @param <T>      类型
     * @return 完全相等
     */
    public static <T> boolean equals(List<T> expected, List<T> actual) {
        if (expected == actual) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (expected.size() != actual.size()) {
            return false;
        }

        List<T> tempList = ListUtils.adaptIterable(expected);
        tempList.removeAll(actual);
        return tempList.isEmpty();
    }

    public static <T> boolean equals(Iterable<T> expected, Iterable<T> actual) {
        return equals(adaptIterable(expected), adaptIterable(actual));
    }

    /**
     * 直接从List中收集对象
     *
     * @param list            list集合
     * @param collectFunction 收集函数
     * @param <T>             集合类型
     * @param <R>             目标类型
     * @return 收集的对象集合
     */
    public static <T, R> MutableList<R> collect(Iterable<T> list, Function<T, R> collectFunction) {
        return adaptIterable(list).collect(collectFunction::apply);
    }

    /**
     * 直接从items中收集对象
     *
     * @param items           items数组
     * @param collectFunction 收集函数
     * @param <T>             集合类型
     * @param <R>             目标类型
     * @return 收集的对象集合
     */
    public static <T, R> MutableList<R> collect(T[] items, Function<T, R> collectFunction) {
        return adaptItems(items).collect(collectFunction::apply);
    }

    /**
     * 将集合拼接成String，抽取其中的要素
     *
     * @param list     集合
     * @param function 抽取字符函数
     * @param <T>      类型
     * @return 字符串
     */
    public static <T> String makeString(Iterable<T> list, Function<T, String> function, String... separator) {
        if (ObjectUtil.isNullOrEmpty(adaptIterable(list))) {
            return null;
        }

        String defaultSeparator = ",";
        if (separator != null && separator.length > 0) {
            defaultSeparator = separator[0];
        }
        return collect(list, function).makeString(defaultSeparator);
    }

    public static String makeString(Object[] items, String separator) {
        return adaptItems(items).makeString(separator);
    }

    /**
     * 拼接字符串，注意，如果有空值，会被过滤
     *
     * @param list list集合
     * @return 收集的对象集合
     */
    public static <T> String makeString(Iterable<T> list, String separator) {
        return adaptIterable(list).makeString(separator);
    }

    /**
     * 跳过 null 值的拼接（注意，"" 空串也算是 null 值
     */
    public static String makeStringSkipNull(String separator, Object... items) {
        return adaptArray(items)
            .distinct()
            .select(Predicates::isNotNull)
            .makeString(separator);
    }

    /**
     * 拼接字符串，注意，如果有空值，会被过滤
     *
     * @param list list集合
     * @return 收集的对象集合
     */
    public static <T> String makeString(Iterable<T> list) {
        return adaptIterable(list).makeString("");
    }

    /**
     * 拼接字符串，注意，如果有空值，会被过滤
     *
     * @param array 数组
     * @return 收集的对象集合
     */
    public static String makeString(String separator, Object... array) {
        return adaptArray(array).makeString(separator);
    }

    /**
     * 截取集合的一部分
     *
     * @param list 集合
     * @param size 截取大小
     * @param <T>  类型
     * @return 子集
     */
    public static <T> List<T> subList(List<T> list, int size) {
        MutableList<T> listCopy = adaptIterable(list);
        if (listCopy.size() >= size) {
            return listCopy.subList(0, size);
        }

        return listCopy;
    }

    /**
     * 安全获取子集合
     *
     * @param list  集合（可为null）
     * @param begin 开始
     * @param end   结束
     * @param <E>   类型
     * @return 子集合
     */
    public static <E> List<E> subList(List<E> list, int begin, int end) {
        MutableList<E> es = adaptIterable(list);
        if (begin > es.size()) {
            return empty();
        }
        end = Math.min(es.size(), end);
        return es.subList(begin, end);
    }

    /**
     * 封装一下toMap方法
     *
     * @param list        集合
     * @param keyFunction 键处理函数
     * @param <X>         原始对象
     * @param <K>         键类型
     * @return map<k, v>
     */
    public static <X, K> MutableMap<K, X> toMap(Iterable<X> list, Function<X, K> keyFunction) {
        return toMap(list, keyFunction, Function.identity());
    }

    /**
     * 封装一下toMap方法
     *
     * @param list          集合
     * @param keyFunction   键处理函数
     * @param valueFunction 值处理函数
     * @param <X>           原始对象
     * @param <K>           键类型
     * @param <V>           值类型
     * @return map<k, v>
     */
    public static <X, K, V> MutableMap<K, V> toMap(Iterable<X> list, Function<X, K> keyFunction, Function<X, V> valueFunction) {
        return adaptIterable(list).toMap(keyFunction::apply, valueFunction::apply);
    }

    /**
     * 转换成一个有序的Map
     */
    public static <T, K extends Comparable<K>, V> LinkedHashMap<K, V> toLinkMap(Collection<T> list, Function<T, K> keyFunction, Function<T, V> valueFunction) {
        LinkedHashMap<K, V> linkedMap = new LinkedHashMap<>();
        forEach(list, x -> linkedMap.put(keyFunction.apply(x), valueFunction.apply(x)));
        return linkedMap;
    }

    /**
     * toMapList 返回 分组集合Map List<T> ---->  Map<K, List<T>>
     */
    public static <T, K extends Comparable<K>> Map<K, List<T>> toMapList(Collection<T> list, Function<T, K> keyFunction) {
        Map<K, List<T>> multiMap = new HashMap<>();
        adaptIterable(list)
            .groupBy(keyFunction::apply)
            .forEachKeyMutableList((k, ts) -> multiMap.put(k, ListUtils.adaptIterable(ts)));
        return multiMap;
    }

    /**
     * toMapList 返回 分组集合Map List<T> ---->  Map<K, List<T>>
     */
    public static <T, K extends Comparable<K>, E> Map<K, List<E>> toMapList(Collection<T> list, Function<T, K> keyFunction, Function<T, E> valueFunction) {
        Map<K, List<E>> multiMap = new HashMap<>();
        adaptIterable(list)
            .groupBy(keyFunction::apply)
            .forEachKeyMutableList((k, ts) -> multiMap.put(k, ts.collect(valueFunction::apply)));
        return multiMap;
    }

    /**
     * alias for #toMapList()
     */
    public static <T, K extends Comparable<K>> Map<K, List<T>> groupBy(Collection<T> list, Function<T, K> keyFunction) {
        return toMapList(list, keyFunction);
    }

    /**
     * alias for #toMapList()
     */
    public static <T, K extends Comparable<K>, E> Map<K, List<E>> groupBy(Collection<T> list, Function<T, K> keyFunction, Function<T, E> valueFunction) {
        return toMapList(list, keyFunction, valueFunction);
    }


    /**
     * Method adaptItems() alias.
     */
    @SafeVarargs
    public static <T> MutableList<T> of(T... items) {
        return adaptItems(items);
    }


    @SafeVarargs
    public static <T> MutableList<T> adaptItems(T... items) {
        return adaptArray(items);
    }

    /**
     * 对List进行包装，此方法空安全，为null时返回空集合
     *
     * @param list 集合
     * @param <E>  类型
     * @return 返回ec中的 MutableList
     */
    public static <E> MutableList<E> adaptIterable(Iterable<E> list) {
        if (list == null) {
            return empty();
        }

        return Lists.mutable.ofAll(list);
    }

    /**
     * 对List进行包装，此方法空安全，为null时返回空集合
     *
     * @param enumeration 集合
     * @param <E>         类型
     * @return 返回ec中的 MutableList
     */
    public static <E> MutableList<E> adaptEnumeration(Enumeration<E> enumeration) {
        MutableList<E> list = empty();
        forEach(enumeration, list::add);
        return list;
    }

    /**
     * 对数组进行包装，此方法空安全，为null时返回空集合
     *
     * @param array 数组
     * @param <T>   类型
     * @return 返回ec中的 MutableList
     */
    public static <T> MutableList<T> adaptArray(T[] array) {
        if (ObjectUtil.isNullOrEmpty(array)) {
            return empty();
        }

        return Lists.mutable.of(array);
    }

    /**
     * 展开Map中的entry为一个字符串对象
     *
     * @param map       map
     * @param separator k v 拼接符号
     * @return f(map, ' _ ') --> [k_v, k1_v2]
     */
    public static List<String> flatMap2List(Map<String, Object> map, String separator) {
        return ListUtils.adaptIterable(map.keySet()).sortThis()
            .collect(x -> {
                Object value = map.get(x);
                if (value == null) {
                    value = "";
                }
                return String.join(separator, x, value.toString());
            });
    }

    @SafeVarargs
    public static <E> MutableList<E> merge(List<E> list, List<E>... lists) {
        MutableList<E> result = adaptIterable(list);
        adaptArray(lists).forEach(x -> {
            if (isNotEmpty(x)) {
                result.addAll(x);
            }
        });
        return result;
    }

    /**
     * 忽略大小写的包含
     *
     * @param list 集合
     * @param item 字符串
     * @return 集合中任何一个包含则为：true，反之为：false
     */
    public static boolean containsIgnoreCase(Iterable<String> list, String item) {
        MutableList<String> newList = adaptIterable(list);

        //两类特殊情况

        //直接包含
        if (newList.contains(item)) {
            return true;
        }

        //对象为空
        if (item == null) {
            return false;
        }

        return newList.anySatisfy(x -> x.equalsIgnoreCase(item));
    }

    /**
     * 不包含语义。以免忘记写 ! 出现的低级错误
     *
     * @param collection 集合
     * @param item       对象
     */
    public static <E> boolean notContains(Collection<E> collection, E item) {
        return !adaptIterable(collection).contains(item);
    }

    public static <T> MutableList<T> empty() {
        return Lists.mutable.empty();
    }

    public static <T> void conditionForEach(List<T> list, Predicate<T> predicate, Consumer<T> consumer) {
        select(list, predicate).forEach(consumer);
    }

    public static <E> void forEach(Enumeration<E> enumeration, Consumer<E> consumer) {
        while (enumeration.hasMoreElements()) {
            E element = enumeration.nextElement();
            consumer.accept(element);
        }
    }

    public static <E> void forEach(Collection<E> list, Consumer<E> consumer) {
        adaptIterable(list).forEach(consumer::accept);
    }

    /**
     * 带下标的循环
     */
    public static <E> void forEachWithIndex(Collection<E> list, MultipleConsumer<E, Integer> consumer) {
        List<E> es = adaptIterable(list);
        for (int i = 0; i < es.size(); i++) {
            consumer.accept(es.get(i), i);
        }
    }

    public static <E> boolean containsAny(Collection<E> values, Collection<E> containsList) {
        //防止其为空
        values = adaptIterable(values);
        if (ObjectUtil.isNullOrEmpty(containsList)) {
            return false;
        }

        return adaptIterable(containsList).anySatisfy(values::contains);
    }

    /**
     * 安全的获取，当集合为空，或者下标不存在时，不抛出异常
     */
    public static <T> T safeGet(List<T> list, int i) {
        try {
            return adaptIterable(list).get(i);
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T safeGetFirst(Iterable<T> list) {
        return adaptIterable(list).getFirst();
    }

    public static <T> T safeGetLast(List<T> list) {
        return adaptIterable(list).getLast();
    }

    /**
     * 向一个可能为空的集合添加元素，如果为空，则创建一个新集合
     *
     * @param nullAbleList 集合
     * @param ts           需要合并的元素
     * @param <T>          集合元素的类型
     * @return 合并后的集合
     */
    @SafeVarargs
    public static <T> List<T> safeAdd(List<T> nullAbleList, T... ts) {
        if (nullAbleList == null) {
            nullAbleList = empty();
        }
        nullAbleList.addAll(adaptArray(ts));
        return nullAbleList;
    }

    /**
     * 根据指定的方式，解析字符串为一个特定的数组
     * <p>
     * 注意：
     * - 如果转换抛出异常，会抛弃
     * - 如果为空，会抛弃
     *
     * @param value           字符串
     * @param delimiter       分隔符号
     * @param convertFunction 转换函数
     * @param <E>             类型
     * @return 指定类型的集合
     */
    public static <E> MutableList<E> parseString2List(String value, String delimiter, Function<String, E> convertFunction) {
        if (ObjectUtil.isNullOrEmpty(value)) {
            return empty();
        }

        return adaptArray(value.split(delimiter))
            .collect(x -> {
                try {
                    return convertFunction.apply(x);
                } catch (Exception ex) {
                    return null;
                }
            })
            .reject(Objects::isNull);
    }

    /**
     * 将 List&lt;List&lt;T>> 结构展开成 List&lt;T>
     *
     * @param lists list集合，中间元素为list
     * @param <T>   类型
     * @return 集合
     */
    public static <T> MutableList<T> flatMap(Collection<List<T>> lists) {
        MutableList<T> value = empty();
        adaptIterable(lists).select(ListUtils::isNotEmpty).forEach(value::addAll);
        return value;
    }

    public static <X, K> MapDifference<K, X> difference(Iterable<X> left, Iterable<X> right, Function<X, K> keyFunction) {
        Map<K, X> leftMap = ListUtils.toMap(left, keyFunction);
        Map<K, X> rightMap = ListUtils.toMap(right, keyFunction);

        return Maps.difference(leftMap, rightMap, new Equivalence<X>() {
            @Override
            protected boolean doEquivalent(X a, X b) {
                // 使用keyFunction进行比较对象在业务上是否是同一个
                return Objects.equals(keyFunction.apply(a), keyFunction.apply(b));
            }

            @Override
            protected int doHash(X x) {
                return Objects.hash(x);
            }
        });
    }

    /**
     * 集合去重
     */
    public <T> MutableList<T> distinct(Iterable<T> list) {
        return adaptIterable(list).distinct();
    }

    public static <T, K, X> ListDiff<T, X> diff(Iterable<T> remotes, Iterable<T> locals, Function<T, X> uniqueKeyFunction, Function<T, K> compareFunction) {
        return diff(remotes, locals, uniqueKeyFunction, compareFunction, null);
    }

    public static <T, K, X> ListDiff<T, X> diff(Iterable<T> remotes, Iterable<T> locals, Function<T, X> uniqueKeyFunction, Function<T, K> compareFunction, MultipleConsumer<T, T> updateConsume) {
        ListDiff<T, X> listDiff = new ListDiff<>();

        //如果远程为空，全是删除
        if (isEmpty(remotes)) {
            listDiff.setDeletes(adaptIterable(locals));
            return listDiff;
        }

        //如果本地为空，则是新增
        if (isEmpty(locals)) {
            listDiff.setAdds(adaptIterable(remotes));
            return listDiff;
        }

        //按唯一键进行 diff 操作
        MapDifference<X, T> difference = difference(remotes, locals, uniqueKeyFunction);

        //=====================赋予差异对象明确的含义======================

        //1-仅远程存在，说明是全新的对象，要保存
        MutableList<T> news = adaptIterable(difference.entriesOnlyOnLeft().values());
        //2-仅在本地存在的对象，远程已经没了，要删除
        MutableList<T> deletes = adaptIterable(difference.entriesOnlyOnRight().values());
        //3-都存在的数据，会丢失掉一个对象，只存：uniqueKey
        MutableList<X> commons = collect(adaptIterable(difference.entriesInCommon().values()), uniqueKeyFunction);
        MutableMap<X, T> remoteMap = toMap(remotes, uniqueKeyFunction);
        MutableMap<X, T> localMap = toMap(locals, uniqueKeyFunction);
        //远程本地都有的数据，则要进行比较，不相同则要更新
        MutableList<T> updates = empty();
        commons.forEach(uniqueKey -> {
            T remote = remoteMap.get(uniqueKey);
            T local = localMap.get(uniqueKey);
            if (!Objects.equals(compareFunction.apply(remote), compareFunction.apply(local))) {
                updates.add(remote);
                if (updateConsume != null) {
                    updateConsume.accept(remote, local);
                }
            }
        });

        listDiff.setCommons(commons);
        listDiff.setAdds(news);
        listDiff.setUpdates(updates);
        listDiff.setDeletes(deletes);

        return listDiff;
    }

    /**
     * 递归收集器
     *
     * @param seed           种子
     * @param kvMap          种子与对象 Map
     * @param parentFunction 下一个种子获取函数
     * @param vxFunction     对象与结果转换函数
     */
    public static <K, V, X> List<X> recursiveCollect(K seed, Map<K, V> kvMap, Function<V, K> parentFunction, Function<V, X> vxFunction) {
        List<X> xList = ListUtils.empty();
        if (!kvMap.containsKey(seed)) {
            return xList;
        }

        doRecursiveCollect(seed, kvMap, parentFunction, vxFunction, xList);

        return xList;
    }

    private static <K, X, V> void doRecursiveCollect(K seed, Map<K, V> kvMap, Function<V, K> parentFunction, Function<V, X> vxFunction, List<X> xList) {
        V v = kvMap.get(seed);
        if (v == null) {
            return;
        }

        xList.add(vxFunction.apply(v));

        //下一个
        seed = parentFunction.apply(v);

        doRecursiveCollect(seed, kvMap, parentFunction, vxFunction, xList);
    }

    /**
     * 循环指定次数获取对象
     *
     * @param i 次数
     */
    public static <T> List<T> repeat(int i, IntFunction<T> function) {
        List<T> list = empty();
        IntStream.range(0, i).forEach(ii -> list.add(function.apply(ii)));
        return list;
    }

    /**
     * 克隆一个List为 List
     */
    public static <T> List<T> clone(List<T> list) {
        return adaptIterable(list);
    }

    /**
     * 去除null后，取最小值，没有返回null
     */
    @SafeVarargs
    public static <T> T min(Comparator<T> comparator, T... ts) {
        return ListUtils.rejectNullAndReduce((t1, t2) -> comparator.compare(t1, t2) < 0 ? t1 : t2, ts);
    }

    /**
     * 去除null后，取最大值，没有返回null
     */
    @SafeVarargs
    public static <T> T max(Comparator<T> comparator, T... ts) {
        return ListUtils.rejectNullAndReduce((t1, t2) -> comparator.compare(t1, t2) > 0 ? t1 : t2, ts);
    }

    @SafeVarargs
    public static <T> T rejectNullAndReduce(BinaryOperator<T> accumulator, T... ts) {
        return ListUtils.adaptArray(ts).select(Objects::nonNull).reduce(accumulator).orElse(null);
    }

    /**
     * 集合值分组，重复Key对Value进行二元操作
     *
     * @param list                list集合
     * @param keyFunction         转换函数
     * @param valueFunction       转换函数
     * @param valueBinaryOperator 二元操作函数
     * @param <X>
     * @param <K>
     * @param <V>
     * @return
     */
    public static <X, K, V> MutableMap<K, V> binary(Iterable<X> list, Function<X, K> keyFunction, Function<X, V> valueFunction, BinaryOperator<V> valueBinaryOperator) {
        MutableMap<K, V> resultMap = org.eclipse.collections.impl.factory.Maps.mutable.empty();
        Iterate.groupBy(list, keyFunction::apply)
            .forEachKeyMultiValues((key, results) -> resultMap.put(key,
                ListUtils.collect(ListUtils.adaptIterable(results), valueFunction)
                    .reduce(valueBinaryOperator)
                    .orElse(null)));
        return resultMap;
    }

    /**
     * 对两个集合取交集，空安全方法
     */
    public <T> List<T> intersection(Iterable<T> x1, Iterable<T> x2) {
        if (Predicates.anyNull(x1, x2)) {
            return ListUtils.empty();
        }

        // 必定二者均不为null
        MutableList<T> xx = adaptIterable(x1);
        xx.retainAll(adaptIterable(x2));

        return xx;
    }

    /**
     * 文本类型拼接in 语句
     * @param items
     * @return
     */
    public static String toTextIn(String[] items) {
        return toInExpr(adaptArray(items), item -> "'" + item + "'", true);
    }

    /**
     * 数字类型拼接in 语句
     * @param items
     * @return
     */
    public static String toNumberIn(Object[] items) {
        return toInExpr(adaptArray(items), ListUtils::toNumberString, true);
    }

    /**
     * 数字类型in元素校验与转换：仅接受 Number 或纯数字字符串（支持负数/小数），
     * 其余类型或非法内容抛出 UtilException，防止拼接出非法 SQL
     * @param item 单个元素
     * @return 数字字面量字符串
     */
    private static String toNumberString(Object item) {
        if (item instanceof Number) {
            return item.toString();
        }
        if (item instanceof String) {
            String value = ((String) item).trim();
            if (value.matches("-?\\d+(\\.\\d+)?")) {
                return value;
            }
        }
        throw new UtilException("toNumberIn 参数必须为数字或数字字符串: " + item);
    }

    public static <T> String toInExpr(List<T> list, Function<T, String> valueFormatFunction, boolean withBrackets) {
        // 移除空元素
        list.removeIf(Predicates::isNull);

        String expr = ListUtils.collect(list, valueFormatFunction).makeString(",");

        return withBrackets ? StringUtils.wrapperWithBrackets(expr) : expr;
    }

    @Setter
    @Getter
    public static class ListDiff<T, X> {

        private List<X> commons = empty();
        private List<T> adds = empty();
        private List<T> deletes = empty();
        private List<T> updates = empty();

        public List<X> getNewsUniqueKeys(Function<T, X> uniqueFunction) {
            return collect(adds, uniqueFunction);
        }

        public List<X> getDeletesUniqueKeys(Function<T, X> uniqueFunction) {
            return collect(deletes, uniqueFunction);
        }

        public List<X> getUpdatesUniqueKeys(Function<T, X> uniqueFunction) {
            return collect(updates, uniqueFunction);
        }

        public boolean isEmpty() {
            return ListUtils.isEmpty(adds) && ListUtils.isEmpty(deletes) && ListUtils.isEmpty(updates);
        }

        public List<T> getAddOrUpdate() {
            List<T> addOrUpdates = adaptIterable(adds);
            addOrUpdates.addAll(updates);
            return addOrUpdates;
        }
    }

    /**
     * 获取Long类型的列表
     * @param ids
     * @return
     */
    public static Long[] getLongListByStringList(String[] ids){
        if (ids.length == 0) {
            throw new RuntimeException("请选择数据");
        }
        return Convert.convert(Long[].class, ids);
    }



    /**
     * 对比两个List<String> 是否全等
     * @param list1 list1
     * @param list2 list2
     * @return boolean
     */
    public static boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            return list1 == list2;
        }
        if (list1.size() != list2.size()) {
            return false;
        }

        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        return set1.equals(set2);
    }

    /**
     * 判断list1中的所有元素是否都在list2中
     * @param list1 list1
     * @param list2 list2
     * @return boolean
     */
    public static boolean firstListItemAllInSecondList(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            return false;
        }
        Set<String> set2 = new HashSet<>(list2);
        for (String item : list1) {
            if (!set2.contains(item)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 把List<Object> 转换为List<Map<String, Object>>
     *
     * @param loadMap
     * @return 树形结构
     */
    public Map<String, List<Map<String, Object>>> listObjectToTree(Map<String, List<Object>> loadMap) {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        loadMap.forEach((k, v) -> {
            ArrayList<Map<String, Object>> list = new ArrayList<>();
            for (Object o : v) {
                list.add(BeanUtil.beanToMap(o));
            }
            map.put(k, list);
        });
        return map;
    }

}
