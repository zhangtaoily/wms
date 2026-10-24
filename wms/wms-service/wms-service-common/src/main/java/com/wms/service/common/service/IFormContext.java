package com.wms.service.common.service;

import com.wms.common.api.BasicBO;
import com.wms.common.enums.BehaviorEnum;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;
import com.wms.domain.WfConfig;
import com.wms.mapper.CustomizeMapper;

import java.util.List;
import java.util.Map;

public interface IFormContext {
    TableNode fetchTableNode();

    FunctionEnum getSupportType();

    CustomizeMapper supplierMapper();

    Tuple2<Integer, String> beforeSubmit(Long id, BasicBO bo);

    void afterSubmit(Long id);

    /**
     * 是否跳过OA审批
     *
     * @return
     */
    Boolean isIgnoreOa(Long id);
    /**
     * 加载
     * @param id
     * @return
     */
    Map<String, List<Map<String, Object>>> doLoad(Long id);

    void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData);

    void afterPassStateChanged(Long id, TableNode root, Map<String, Object> bo);

    void afterPass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData, Map<String, List<Map<String, Object>>> dbNewData);

    void afterRejectStateChanged(Long id, TableNode root, Map<String, Object> bo);

    void afterReject(Long id);

    Tuple2<Boolean, String> checkRollBack(Long id);

    Tuple2<Boolean, String> beforeRollBack(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbData);

    Tuple2<Boolean, String> afterRollBack(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbData);

    void batchChangeCancelOaStatus(Long[] ids, Integer state);

    Tuple2<Boolean, String> batchChangeCancelStatus(Long[] ids, Integer isCancel, Map<String, Object> bo);

    void batchChangeDetailsCancelOaStatus(Long[] ids, Integer state);

    Tuple2<Boolean, String> batchChangeDetailsCancelStatus(Long[] ids, Integer isCancel, Map<String, Object> bo);

    Tuple2<Boolean, String> writeBack(TableNode root, FunctionEnum functionEnum, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Map<String, Object>>> newTableEntityMap, BehaviorEnum behaviorEnum);
}
