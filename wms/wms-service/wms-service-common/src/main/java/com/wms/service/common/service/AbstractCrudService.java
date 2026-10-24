package com.wms.service.common.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wms.common.core.domain.BaseEntity;
import com.wms.common.core.domain.IdEntity;
import com.wms.common.utils.ListUtils;
import com.wms.mapper.CustomizeMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

/**
 * 常规的CRUD操作
 *
 * @param <T> 实体类型
 * @param <M> mapper类型
 */
@Slf4j
public abstract class AbstractCrudService<T extends IdEntity, M extends CustomizeMapper<T>> {

    /**
     * 子类注入mapper对象
     *
     * @return mapper
     */
    public abstract M supplierMapper();

    public long count(String sql) {
        return supplierMapper().count(sql);
    }

    public long count(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> queryWrapper = lambdaQuery();
        consumer.accept(queryWrapper);
        return count(queryWrapper);
    }

    public long count(Wrapper<T> queryWrapper) {
        return supplierMapper().selectCount(queryWrapper);
    }

    /**
     * 保存，并且获取保存对象本身，会产生缓存
     *
     * @param t 保存参数
     * @return 存储后的对象
     */
    public T save(T t) {
        if (t instanceof BaseEntity) {
            // 保存的时候设置更新时间，使缓存对象里拥有更新时间
            ((BaseEntity) t).setUpdateTime(LocalDateTime.now());
        }

        supplierMapper().insert(t);

        return t;
    }

    /**
     * 批量插入方法，注意：此处采用循环调单条插入的方法实现
     */
    public void saveBatch(List<T> list) {
        if (ListUtils.isEmpty(list)) {
            return;
        }

        list.forEach(supplierMapper()::insert);
    }

    /**
     * 根据ID获取
     *
     * @param id 表ID
     * @return 实体
     */
    public T get(Long id) {
        return supplierMapper().selectById(id);
    }

    public T get(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> lambdaQuery = lambdaQuery();
        consumer.accept(lambdaQuery);

        //如果没有声明禁用缓存，则需要从缓存中清理
        M mapper = supplierMapper();

        return mapper.selectOne(lambdaQuery);
    }

    /**
     * 更新对象，将会移除缓存
     *
     * @param t 实体对象
     */
    public int update(T t) {
        if (t instanceof BaseEntity) {
            BaseEntity at = (BaseEntity) t;
            // 更新的时候设置更新时间，如果所有值未发生变化，mysql不会触发更新时间
            at.setUpdateTime(LocalDateTime.now());
        }
        return supplierMapper().updateById(t);
    }

    public int updateBatch(List<T> tList) {
        int update = 0;
        for (T t : tList) {
            update += update(t);
        }
        return update;
    }


    public int update(Long id, Consumer<LambdaUpdateWrapper<T>> updateWrapperConsumer) {
        LambdaUpdateWrapper<T> updateWrapper = lambdaUpdate();
        updateWrapperConsumer.accept(updateWrapper);
        //由于mybatis-plus 无法识别父类的字段表达式：IdAutoEntity::getId，因此，需要明确的告诉它，当前是哪个类
        updateWrapper.setEntityClass(supplierMapper().supportEntityType());
        updateWrapper.eq(IdEntity::getId, id);
        return supplierMapper().update(null, updateWrapper);
    }

    /**
     * 按条件更新，并且会处理缓存
     *
     * @param setWrapperConsumer   更新字段语句
     * @param whereWrapperConsumer 条件语句
     */

    public void updateBatch(Consumer<LambdaUpdateWrapper<T>> setWrapperConsumer, Consumer<LambdaUpdateWrapper<T>> whereWrapperConsumer) {
        //直接使用 wrapper 更新
        LambdaUpdateWrapper<T> wrapper = lambdaUpdate();
        whereWrapperConsumer.accept(wrapper);

        List<T> users = listOnlyIdField(wrapper);
        if (ListUtils.isEmpty(users)) {
            return;
        }

        M mapper = supplierMapper();

        //执行更新
        setWrapperConsumer.accept(wrapper);
        mapper.update(null, wrapper);
    }

    private List<T> listOnlyIdField(LambdaUpdateWrapper<T> wrapper) {
        String tableName = TableInfoHelper.getTableInfo(supplierMapper().supportEntityType()).getTableName();
        return supplierMapper().selectIds(tableName, wrapper);
    }

    public int deleteBatchIds(List<String> id) {
        return supplierMapper().deleteBatchIds(id);
    }

    public int delete(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> lambdaQuery = lambdaQuery();
        consumer.accept(lambdaQuery);

        //如果没有声明禁用缓存，则需要从缓存中清理
        M mapper = supplierMapper();

        return mapper.delete(lambdaQuery);
    }

    /**
     * 清空表
     */
    public void truncate() {
        String tableName = TableInfoHelper.getTableInfo(supplierMapper().supportEntityType()).getTableName();
        supplierMapper().truncate(tableName);
    }

    /**
     * 清空表数据 (可被事务回滚)
     */
    public void deleteTableData() {
        String tableName = TableInfoHelper.getTableInfo(supplierMapper().supportEntityType()).getTableName();
        supplierMapper().deleteTableData(tableName);
    }

    public <P extends IPage<T>> P page(P page) {
        return page(page, emptyQuery());
    }

    /**
     * 设置条件后，进行分页查询
     *
     * @param page 分页条件
     * @return 分页查询
     */
    public <P extends IPage<T>> P page(P page, Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> query = lambdaQuery();
        consumer.accept(query);
        return page(page, query);
    }

    /**
     * 根据查询条件进行分页查询
     *
     * @param page    分页条件
     * @param wrapper 查询条件
     * @return 分页查询
     */
    public <P extends IPage<T>> P page(P page, Wrapper<T> wrapper) {
        return supplierMapper().selectPage(page, wrapper);
    }

    public QueryWrapper<T> emptyQuery() {
        return new QueryWrapper<>();
    }

    public List<T> list(Wrapper<T> wrapper) {
        return supplierMapper().selectList(wrapper);
    }

    public List<T> list() {
        return supplierMapper().selectList(lambdaQuery());
    }


    public List<T> list(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> queryWrapper = lambdaQuery();
        consumer.accept(queryWrapper);
        return list(queryWrapper);
    }


    public LambdaQueryWrapper<T> lambdaQuery() {
        return Wrappers.lambdaQuery();
    }
}
