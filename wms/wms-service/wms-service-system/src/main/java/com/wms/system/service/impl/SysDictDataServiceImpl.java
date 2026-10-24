package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.constant.CacheNames;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.entity.SysDictData;
import com.wms.common.core.domain.entity.SysDictType;
import com.wms.common.core.page.TableDataInfo;
import com.wms.common.exception.ServiceException;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import com.wms.common.utils.Tuple2;
import com.wms.common.utils.redis.CacheUtils;
import com.wms.system.domain.vo.DictDataVo;
import com.wms.system.mapper.SysDictDataMapper;
import com.wms.system.mapper.SysDictTypeMapper;
import com.wms.system.service.ISysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class SysDictDataServiceImpl implements ISysDictDataService {

    @Resource
    private SysDictDataMapper baseMapper;
    @Resource
    private SysDictTypeMapper sysDictTypeMapper;

    @Override
    public TableDataInfo<SysDictData> selectPageDictDataList(SysDictData dictData, PageQuery pageQuery) {
        LambdaQueryWrapper<SysDictData> lqw = new LambdaQueryWrapper<SysDictData>()
            .eq(StringUtils.isNotBlank(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
            .like(StringUtils.isNotBlank(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel())
            .eq(StringUtils.isNotBlank(dictData.getStatus()), SysDictData::getStatus, dictData.getStatus())
            .orderByAsc(SysDictData::getDictSort);
        Page<SysDictData> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataList(SysDictData dictData) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysDictData>()
            .eq(StringUtils.isNotBlank(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
            .like(StringUtils.isNotBlank(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel())
            .eq(StringUtils.isNotBlank(dictData.getStatus()), SysDictData::getStatus, dictData.getStatus())
            .orderByAsc(SysDictData::getDictSort));
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
                .select(SysDictData::getDictLabel)
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getDictValue, dictValue))
            .getDictLabel();
    }

    public SysDictData selectDictData(String dictType, String dictValue) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
            .eq(SysDictData::getDictType, dictType)
            .eq(SysDictData::getDictValue, dictValue));
    }

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    @Override
    public SysDictData selectDictDataById(Long dictCode) {
        return baseMapper.selectById(dictCode);
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    @Override
    public void deleteDictDataByIds(Long[] dictCodes) {
        for (Long dictCode : dictCodes) {
            SysDictData data = selectDictDataById(dictCode);
            baseMapper.deleteById(dictCode);
            CacheUtils.evict(CacheNames.SYS_DICT, data.getDictType());
        }
    }

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public List<SysDictData> insertDictData(SysDictData data) {
        int row = baseMapper.insert(data);
        if (row > 0) {
            return baseMapper.selectDictDataByType(data.getDictType());
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public List<SysDictData> updateDictData(SysDictData data) {
        int row = baseMapper.updateById(data);
        if (row > 0) {
            return baseMapper.selectDictDataByType(data.getDictType());
        }
        throw new ServiceException("操作失败");
    }

    @Override
    public List<SysDictData> selectDictDataListByDictType(String dictType) {

        return baseMapper.selectList(new LambdaQueryWrapper<SysDictData>()
            .eq(SysDictData::getDictType, dictType)
            .eq(SysDictData::getStatus, "0")
            .orderByAsc(SysDictData::getDictSort));
    }


    /**
     * 修改数据字典，字典类型不支持新增，支持项的新增、修改、删除。
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, Object> upsert(@Valid DictDataVo vo) {
        String dictType = vo.getDictType();
        if (StringUtils.isBlank(dictType)) {
            return Tuple2.of(false, "字典类型不能为空");
        }

        // 字典类型必须已存在，不支持新增字典类型
        Long typeCount = sysDictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
            .eq(SysDictType::getDictType, dictType));
        if (typeCount == null || typeCount <= 0) {
            return Tuple2.of(false, "字典类型[" + dictType + "]不存在，不支持新增字典类型");
        }

        List<DictDataVo.DictDataItem> items = vo.getDictItems();
        if (items == null) {
            items = new ArrayList<>();
        }

        // 校验字典项键值
        for (DictDataVo.DictDataItem item : items) {
            if (StringUtils.isBlank(item.getDictValue())) {
                return Tuple2.of(false, "字典键值不能为空");
            }
        }

        // 查询该类型下全部现有项(含停用)，按 dictValue 建立索引
        List<SysDictData> existingList = baseMapper.selectList(new LambdaQueryWrapper<SysDictData>()
            .eq(SysDictData::getDictType, dictType));
        Map<String, SysDictData> existingMap = new HashMap<>();
        for (SysDictData data : existingList) {
            existingMap.put(data.getDictValue(), data);
        }

       SysDictType sysDictType = sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
            .eq(SysDictType::getDictType, dictType));

        // 传入项的 dictValue 集合(用于后续判断删除)
        Set<String> incomingValues = new HashSet<>();

        // 新增 / 修改
        int sort = 0;
        for (DictDataVo.DictDataItem item : items) {
            sort++;
            String dictValue = item.getDictValue();
            String dictLabel = item.getDictLabel();
            incomingValues.add(dictValue);

            SysDictData existing = existingMap.get(dictValue);
            if (existing != null) {
                // 修改：仅更新 label/sort，保留原 status 等字段
                existing.setDictLabel(dictLabel);
                existing.setDictSort(sort);
                baseMapper.updateById(existing);
            } else {
                // 新增
                SysDictData data = new SysDictData();
                data.setDictType(dictType);
                data.setDictLabel(dictLabel);
                data.setDictValue(dictValue);
                data.setDictSort(sort);
                data.setStatus("0");
                data.setDictTypeId(sysDictType.getId());
                baseMapper.insert(data);
                // 放入索引，避免传入列表内重复 dictValue 导致重复新增
                existingMap.put(dictValue, data);
            }
        }

        // 清缓存
        CacheUtils.evict(CacheNames.SYS_DICT, dictType);

        return Tuple2.of(true, null);
    }

    /**
     * 启用
     * @param vo
     * @return
     */
    public Tuple2<Boolean, Object> enable(@Valid DictDataVo vo) {
        List<SysDictData> existingList = baseMapper.selectList(new LambdaQueryWrapper<SysDictData>()
            .eq(SysDictData::getDictType, vo.getDictType())
            .in(SysDictData::getDictValue, ListUtils.collect(vo.getDictItems(), DictDataVo.DictDataItem::getDictValue))
        );

       Map<String,Integer> dictMap= ListUtils.toMap(vo.getDictItems(), DictDataVo.DictDataItem::getDictValue, DictDataVo.DictDataItem::getEnable);

        for (SysDictData data : existingList) {

            if(dictMap.containsKey(data.getDictValue())){
                data.setStatus(dictMap.get(data.getDictValue()).toString().equals("1") ?"0":"1");
                baseMapper.updateById(data);
            }
        }
        return Tuple2.of(true, null);
    }

}


