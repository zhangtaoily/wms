package com.wms.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.constant.UserConstants;
import com.wms.common.core.domain.entity.SysDictData;
import com.wms.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 字典表 数据层
 *
 * @author Lion Li
 */
@Mapper
public interface SysDictDataMapper extends BaseMapperPlus<SysDictDataMapper, SysDictData, SysDictData> {

    default List<SysDictData> selectDictDataByType(String dictType) {
        return selectList(
            new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getStatus, UserConstants.DICT_NORMAL)
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort));
    }
}
