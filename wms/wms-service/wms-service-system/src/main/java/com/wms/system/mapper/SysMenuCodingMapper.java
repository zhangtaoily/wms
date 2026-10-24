package com.wms.system.mapper;

import com.wms.domain.SysMenuCoding;
import com.wms.service.common.vo.SysMenuCodingVo;
import com.wms.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 菜单编码Mapper接口
 *
 * @author ruoyi
 * @date 2024-08-16
 */
public interface SysMenuCodingMapper extends BaseMapperPlus<SysMenuCodingMapper, SysMenuCoding, SysMenuCodingVo> {

    @Select("${nativeSql}")
    String nativeSql(@Param("nativeSql") String nativeSql);

}
