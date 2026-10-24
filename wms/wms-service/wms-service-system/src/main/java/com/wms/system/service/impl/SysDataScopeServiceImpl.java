package com.wms.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.core.domain.entity.SysDept;
import com.wms.common.helper.DataBaseHelper;
import com.wms.common.utils.StreamUtils;
import com.wms.domain.SysRoleDept;
import com.wms.system.mapper.SysDeptMapper;
import com.wms.system.mapper.SysRoleDeptMapper;
import com.wms.system.service.ISysDataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限 实现
 * <p>
 * 注意: 此Service内不允许调用标注`数据权限`注解的方法
 * 例如: deptMapper.selectList 此 selectList 方法标注了`数据权限`注解 会出现循环解析的问题
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service("sdss")
public class SysDataScopeServiceImpl implements ISysDataScopeService {

    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;

    @Override
    public String getRoleCustom(Long roleId) {
        List<SysRoleDept> list = roleDeptMapper.selectList(
            new LambdaQueryWrapper<SysRoleDept>()
                .select(SysRoleDept::getDeptId)
                .eq(SysRoleDept::getRoleId, roleId));
        if (CollUtil.isNotEmpty(list)) {
            return StreamUtils.join(list, rd -> Convert.toStr(rd.getDeptId()));
        }
        return null;
    }

    @Override
    public String getDeptAndChild(Long deptId) {
        List<SysDept> deptList = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
            .select(SysDept::getId)
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
        List<Long> ids = StreamUtils.toList(deptList, SysDept::getId);
        ids.add(deptId);
        if (CollUtil.isNotEmpty(ids)) {
            return StreamUtils.join(ids, Convert::toStr);
        }
        return null;
    }

}
