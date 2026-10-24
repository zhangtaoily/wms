package com.wms.base.service.doc;

import com.wms.common.api.BasicBO;
import com.wms.common.constant.SqlConstant;
import com.wms.common.core.domain.IdEntity;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.common.utils.StringUtils;
import com.wms.domain.system.SysAutoCodeSet;
import com.wms.mapper.CustomizeMapper;
import com.wms.base.service.BaseWmsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.spi.ISysAutoCodeSetService;

import java.time.LocalDate;
import java.util.Map;

/**
 * M3 入库单据域服务基类
 * 引擎 fetchCode 的 MAX(code) 写死编号列为 code，M3 单据编号列各不相同，
 * 此处覆写为按 codeColumn() 取号；前缀占位符支持 [yyyy]/[yy]/[mm]/[dd]
 */
public abstract class AbstractM3DocService<E extends IdEntity, M extends CustomizeMapper<E>> extends BaseWmsFormService<E, M> {

    /** 单据编号列名（如 code_no/pallet_order_no/inbound_no）；无编号列返回 null */
    protected abstract String codeColumn();

    @Override
    public String fetchCode(BasicBO bo) {
        String column = codeColumn();
        if (StringUtils.isEmpty(column)) {
            return null;
        }
        SysAutoCodeSet autoCodeSet = ApplicationContextHolder.getBean(ISysAutoCodeSetService.class).getAutoCodeSet(getSupportType());
        if (autoCodeSet == null) {
            return null;
        }

        TableNode root = fetchTableNode();
        String masterCode = buildMasterCode(autoCodeSet.getPrefix());
        int length = StringUtils.isEmpty(masterCode) ? autoCodeSet.getSerialLen() : masterCode.length() + autoCodeSet.getSerialLen();
        StringBuilder sql = new StringBuilder();
        sql.append(SqlConstant.SELECT).append("MAX(").append(column).append(") maxCode ");
        sql.append(SqlConstant.FROM).append(root.getTableName());
        sql.append(SqlConstant.WHERE).append(column).append(" NOT LIKE '%-%'");
        if (StringUtils.isNotEmpty(masterCode)) {
            sql.append(SqlConstant.AND).append(column).append(SqlConstant.LIKE).append("'").append(masterCode).append("%'");
        }
        sql.append(SqlConstant.AND).append("length(").append(column).append(") = ").append(length);

        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        Map<String, Object> map = customizeMapper.query(sql.toString());
        String existsCode = map == null ? "" : (String) map.get("maxCode");
        String serialStr;
        if (StringUtils.isEmpty(existsCode)) {
            serialStr = StringUtils.padl("1", autoCodeSet.getSerialLen(), '0');
        } else {
            int serialNum = Integer.parseInt(StringUtils.isEmpty(masterCode) ? existsCode : existsCode.replace(masterCode, ""));
            serialStr = StringUtils.padl(String.valueOf(serialNum + 1), autoCodeSet.getSerialLen(), '0');
        }
        return StringUtils.isEmpty(masterCode) ? serialStr : masterCode + serialStr;
    }

    /** 解析编号前缀占位符：[yyyy]/[YY]→年，[mm]→月，[dd]→日 */
    private String buildMasterCode(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return prefix;
        }
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = StringUtils.padl(String.valueOf(now.getMonthValue()), 2, '0');
        String day = StringUtils.padl(String.valueOf(now.getDayOfMonth()), 2, '0');
        return prefix
            .replace("[yyyy]", year).replace("[YYYY]", year)
            .replace("[yy]", year.substring(2)).replace("[YY]", year.substring(2))
            .replace("[mm]", month).replace("[MM]", month)
            .replace("[dd]", day).replace("[DD]", day);
    }
}
