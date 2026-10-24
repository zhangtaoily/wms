package com.wms.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.experimental.UtilityClass;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public final class PageUtils {

    /**
     * 对页数据进行收集，并且排除空值，结果为集合
     */
    public static <T, X> MutableList<X> collectWithRejectNull(IPage<T> tPage, Function<T, X> txFunction) {
        return collect(tPage, txFunction).reject(Predicates::isNull);
    }

    /**
     * 对页数据进行收集，结果为集合
     */
    public static <T, X> MutableList<X> collect(IPage<T> tPage, Function<T, X> txFunction) {
        return ListUtils.collect(tPage.getRecords(), txFunction);
    }

    /**
     * 消费页里的每个数据
     */
    public static <T> void consumer(IPage<T> page, Consumer<T> consumer) {
        if (isEmpty(page)) {
            return;
        }

        page.getRecords().forEach(consumer);
    }

    /**
     * 通过原页的分页数据，创建一个空页
     */
    @Deprecated
    public static <T, X> Page<X> transformEmpty(IPage<T> tPage) {
        Page<X> xPage = Page.of(tPage.getCurrent(), tPage.getSize());
        xPage.setRecords(ListUtils.empty());
        return xPage;
    }

    /**
     * 将页数据进行转换，并且可选过滤空
     */
    public static <T, X> Page<X> transform(IPage<T> tPage, Function<T, X> function, boolean rejectNullObject) {
        Page<X> xPage = transform(tPage, function);
        if (rejectNullObject) {
            xPage.getRecords().removeIf(Objects::isNull);
        }
        return xPage;
    }

    /**
     * 将页数据进行转换
     */
    public static <T, X> Page<X> transform(IPage<T> tPage, Function<T, X> function) {
        Page<X> xPage = Page.of(tPage.getCurrent(), tPage.getSize(), tPage.getTotal());
        xPage.setRecords(ListUtils.collect(tPage.getRecords(), function));
        return xPage;
    }

    /**
     * 使用新数据，替换掉原页的数据
     */
    public static <T, X> Page<X> of(IPage<T> tPage, List<X> records) {
        Page<X> xPage = Page.of(tPage.getCurrent(), tPage.getSize());
        xPage.setPages(tPage.getPages());
        xPage.setTotal(tPage.getTotal());
        xPage.setRecords(records);
        return xPage;
    }

    /**
     * 创建一个不包含 total 属性的 Page 对象
     */
    public static <T> Page<T> of(long current, long pageSize, List<T> records) {
        Page<T> page = Page.of(current, pageSize);
        page.setRecords(records);
        return page;
    }

    /**
     * 创建一个 Page 对象
     */
    public static <T> Page<T> of(long current, long size, long total, List<T> records) {
        Page<T> page = of(current, size, records);
        page.setTotal(total);
        return page;
    }


    /**
     * 空页
     */
    public static <T> Page<T> empty(long current, long size) {
        return of(current, size, 0, ListUtils.empty());
    }

    /**
     * 空页
     */
    public static <T> Page<T> empty() {
        return empty(0, 0);
    }

    /**
     * 默认分页对象，页码=1，分页大小=Int.max，相当于不分页
     */
    public static <T> Page<T> nonePage() {
        return Page.of(1, Integer.MAX_VALUE, false);
    }

    /**
     * 判断页是否为空页
     */
    public static boolean isEmpty(IPage<?> page) {
        return page == null || ListUtils.isEmpty(page.getRecords());
    }

    /**
     * 从总集合中进行创建分页数据
     */
    public static <T> Page<T> ofPage(long current, long size, List<T> totalRecords) {
        int begin = (int) ((current - 1) * size);
        int end = begin + (int) size;
        List<T> records = ListUtils.subList(totalRecords, begin, end);
        return of(current, size, totalRecords.size(), records);
    }

    public static void nextPage(IPage<?> page) {
        long current = page.getCurrent();
        page.setCurrent(current + 1);
    }

    /**
     * 获取页大小，null时，返回 0
     */
    public static <X> int sizeOf(Page<X> page) {
        if (isEmpty(page)) {
            return 0;
        }
        return page.getRecords().size();
    }

    public static int calOffset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}
