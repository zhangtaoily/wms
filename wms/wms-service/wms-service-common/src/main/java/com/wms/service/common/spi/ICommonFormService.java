package com.wms.service.common.spi;

import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.vo.PushDownVo;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import com.wms.vo.BatchCancelVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public interface ICommonFormService extends InitializingBean {
    FunctionEnum getSupportType();

    DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery);

    @Transactional(rollbackFor = Exception.class)
    Tuple2<Boolean, String> remove(Long id);

    @Transactional(rollbackFor = Exception.class)
    Tuple2<Boolean, String> batchRemove(Long[] id);

    Tuple2<Integer, Object> save(BasicBO bo);

    Tuple2<Boolean, Object> batchSave(BasicBO bo);

    Tuple2<Boolean, Object> staging(BasicBO bo);

    Tuple2<Integer, String> submit(Long id, BasicBO bo);

    Map<String, Object> init(Long id);

    List<Map<String, Object>> batchInit(List<String> ids);

    /*下推*/
    Map<String, Object> push(String type, List<PushDownVo> pushDowns);

    /*
     * 查询单据明细,没有表头
     */
    List<Map<String, Object>> getDetailList(BasicBO bo);

    Map<String, Object> initBind();

    Tuple2<Boolean, String> pass(Long id, Map<String, Object> bo);

    Tuple2<Boolean, String> reject(Long id, Map<String, Object> bo);

    Tuple2<Boolean, String> rollBack(Long id);

    Tuple2<Boolean, String> check(List<Long> ids);
    Tuple2<Boolean, String> reCheck(List<Long> ids);
    Tuple2<Boolean, String> afterRollBack(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbData);
    Tuple2<Boolean, String> beforeRollBack(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbData);

    /**
     * 关闭单个单据
     *
     * @param id id
     * @return Tuple2
     */
    Tuple2<Boolean, String> close(Long id);

    /**
     * 批量关闭单据
     *
     * @param ids 表头id列表
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchClose(Long[] ids);

    /**
     * 批量关闭单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchCloseDetails(Long[] ids);

    /**
     * 批量反关闭单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchReClose(Long[] ids);

    /**
     * 批量反关闭单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchReCloseDetails(Long[] ids);

    /**
     * 批量取消单据
     *
     * @param vo vo
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchCancel(BatchCancelVo vo);

    /**
     * 批量取消单据明细
     *
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchCancelDetails(BatchCancelVo vo);

    Tuple2<Boolean, String> beforeCancel(BatchCancelVo vo,Boolean isGoods);
    /**
     * 校验能否取消
     * @param vo vo
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> verifyIsAllowCancel(Long id,BatchCancelVo vo);

    /**
     * 校验明细行能否取消
     * @param vo vo
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> verifyGoodsIsAllowCancel(BatchCancelVo vo);

    /**
     * 批量反取消单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchReCancel(Long[] ids);

    /**
     * 批量反取消单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    Tuple2<Boolean, String> batchReCancelDetails(Long[] ids);

    Tuple2<Boolean, Object> copy(Long id, BasicBO bo);

    Tuple2<Boolean, Object> copyAll(Long id, BasicBO bo);

    Boolean muliSend(Long[] ids);

    Boolean muliSend(Long[] ids, int workTiming);

    Boolean allSend();

    R reTry(Long logId);

    @Override
    default void afterPropertiesSet() throws Exception {
        CommonFormManager.getInstance().registerFormList(this);
    }

    Tuple2<Boolean, String> callBack(Map<String, Object> bo);

    Map<String, Object> getDetailListWithHead(BasicBO bo);

    /**
     * 清除表头数据的通用字段
     *
     * @param map
     */
    void refreshMainTableCommonFields(Map<String, Object> map);

    /**
     * 清除子表数据的通用字段
     *
     * @param map
     */
    void refreshSonTableCommonFields(Map<String, Object> map);

    /**
     * 获取对比数据
     *
     * @param sourceId
     * @param beforeChangeId
     * @return
     */
    Map getComparisonData(Long sourceId, Long beforeChangeId);

    /**
     * 加载
     * @param id
     * @return
     */
    Map<String, List<Map<String, Object>>> doLoad(Long id);
}
