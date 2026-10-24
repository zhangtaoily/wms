package com.wms.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.page.TableDataInfo;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.StringUtils;
import com.wms.domain.SysSetting;
import com.wms.system.mapper.SysSettingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 系统设置 业务层
 */
@RequiredArgsConstructor
@Service
public class SysSettingService {

    private final SysSettingMapper baseMapper;

    /**
     * 分页查询系统设置
     */
    public TableDataInfo<SysSetting> pageSetting(SysSetting setting, PageQuery pageQuery) {
        LambdaQueryWrapper<SysSetting> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotBlank(setting.getSettingCode()), SysSetting::getSettingCode, setting.getSettingCode());
        lqw.like(StringUtils.isNotBlank(setting.getSettingName()), SysSetting::getSettingName, setting.getSettingName());
        lqw.orderByAsc(SysSetting::getId);
        Page<SysSetting> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 按ID查询系统设置
     */
    public SysSetting selectSettingById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 校验设置编码是否唯一
     */
    public boolean checkSettingCodeUnique(SysSetting setting) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysSetting>()
            .eq(SysSetting::getSettingCode, setting.getSettingCode())
            .ne(setting.getId() != null, SysSetting::getId, setting.getId()));
        return !exist;
    }

    /**
     * 新增系统设置
     */
    public int insertSetting(SysSetting setting) {
        setting.setCreator(LoginHelper.getUsername());
        return baseMapper.insert(setting);
    }

    /**
     * 修改系统设置
     */
    public int updateSetting(SysSetting setting) {
        // 实体字段直接映射（无自动填充），updateById 全字段进 SET；需回填原 creator/createTime，避免 create_time 写 NULL
        SysSetting db = baseMapper.selectById(setting.getId());
        if (db != null) {
            setting.setCreator(db.getCreator());
            setting.setCreateTime(db.getCreateTime());
        }
        setting.setUpdater(LoginHelper.getUsername());
        if (setting.getUpdateTime() == null) {
            setting.setUpdateTime(java.time.LocalDateTime.now());
        }
        return baseMapper.updateById(setting);
    }

    /**
     * 批量删除系统设置
     */
    public int deleteSettingByIds(Long[] ids) {
        return baseMapper.deleteBatchIds(java.util.Arrays.asList(ids));
    }
}
