
package com.wms.system.service.interfaceManage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.wms.common.core.domain.R;
import com.wms.common.thread.ThreadPool;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.common.utils.HttpHelper;
import com.wms.common.utils.JsonUtils;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysInterfaceLog;
import com.wms.mapper.CustomizeMapper;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.spi.IInterfaceHelper;
import com.wms.system.mapper.InterfaceLogMapper;
import com.wms.system.mapper.SysBusinessflowConfigMapper;
import com.wms.system.service.SysBusinessflowConfigService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InterfaceHelper implements IInterfaceHelper {
    CustomizeMapper customizeMapper;
    //volatile TableNode theNode;
    AtomicBoolean IsError = new AtomicBoolean(false);//接口配置是否报错

    public enum DataType {
        null_d,
        string_d,
        int_d,
        decimal_d,
        boolean_d
    }
    //#region 对外方法

    /**
     * 触发分发
     *
     * @param dbData
     * @param tableNode
     * @param pactId
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Tuple2<Boolean, String> setAPIInfo(Map<String, List<Map<String, Object>>> dbData,
                                              TableNode tableNode,
                                              Long pactId,
                                              int workTiming,
                                              JSONObject customParamJson) {
        String tableName = tableNode.getTableName();//主表表名
        try {
            SysBusinessflowConfigService sysBusinessflowConfigService = ApplicationContextHolder.getBean(SysBusinessflowConfigService.class);
            TableNode businessNode = sysBusinessflowConfigService.fetchTableNode();
            customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(businessNode.getMapper());
            List<Map<String, Object>> apis = new ArrayList<>();
            //#region 根据业务流查询接口
            String sqlStr = "select sys_businessflow_config.id, sys_businessflow_config.send_type,sys_businessflow_config_detail.sort_index, sys_businessflow_config_detail.org_id,sys_businessflow_config_detail.org_source,sys_businessflow_config_detail.system_id\n" +
                ",sys_businessflow_config_detail.purpose_id,sys_businessflow_config_detail.condition_sql from sys_businessflow_config \n" +
                "left join sys_businessflow_config_detail on sys_businessflow_config_detail.business_id=sys_businessflow_config.id\n" +
                "where work_timing='" + workTiming + "' and sys_businessflow_config_detail.enabled='1' and model_id='" + tableName + "'";
            List<Map<String, Object>> businessData = customizeMapper.queryList(sqlStr);
            Map<String, Object> root = dbData.get(tableName).get(0);//表头数据
            for (Map<String, Object> map : businessData) {

                //判断是否满足执行条件
                if (map.get("condition_sql") != null && !map.get("condition_sql").toString().equals("")) {
                    String sql = map.get("condition_sql").toString();//执行条件
                    String pactSql = "select id from " + tableName + " where id='" + pactId + "' and " + sql;
                    List<Map<String, Object>> pactDataId = customizeMapper.queryList(pactSql);
                    if (pactDataId.isEmpty()) {
                        continue;//不符合就跳过
                    }
                }

                String orgId = "";
                int IsSort = 0;
                //业务推送组织从单据上取
                if (map.get("send_type").equals("1")) {
                    IsSort = 1;//业务推送需要按序执行
                    String orgType = map.get("org_source").toString();
                    if (orgType.equals("1")) {
                        //组织取销售组织
                        if (root.containsKey("salOrgId")) {
                            orgId = root.get("salOrgId").toString();
                        } else {
                            //报错
                            ErrorLog(pactId, tableName, "分发异常：业务单据缺少销售组织sal_org_id");
                            return Tuple2.of(false, "分发异常：业务单据缺少销售组织sal_org_id");
                        }
                    } else if (orgType.equals("2")) {
                        //组织取备货组织
                        if (root.containsKey("prdOrgId")) {
                            orgId = root.get("prdOrgId").toString();
                        } else {
                            //报错
                            ErrorLog(pactId, tableName, "分发异常：业务单据缺少备货组织prd_org_id");
                            return Tuple2.of(false, "分发异常：业务单据缺少备货组织prd_org_id");
                        }
                    } else {
                        //库存组织
                        if (root.containsKey("orgId")) {
                            orgId = root.get("orgId").toString();
                        } else {
                            //报错
                            ErrorLog(pactId, tableName, "分发异常：业务单据缺少库存组织org_id");
                            return Tuple2.of(false, "分发异常：业务单据缺少库存组织org_id");
                        }
                    }
                    String orgSql = "select dict_value from sys_dict_data where dict_type='interface_org' and ex_field001='" + orgId + "'";
                    List<Map<String, Object>> orgData = customizeMapper.queryList(orgSql);
                    if (orgData.size() > 0 && orgData.get(0) != null) {
                        orgId = orgData.get(0).get("dict_value").toString();
                    } else {
                        ErrorLog(pactId, tableName, "分发异常：业务单据接收方组织尚未配置");
                        return Tuple2.of(false, "分发异常：业务单据接收方组织尚未配置");
                    }
                } else {
                    orgId = map.get("org_id").toString();
                }
                String systemId = map.get("system_id").toString();
                String purposeId = map.get("purpose_id").toString();
                //根据条件匹配符合的接口
                String apiSql = "select sys_interface_config.id, organization_id,system_id,purpose_id,'" + map.get("sort_index").toString() + "' `index`,'" + IsSort + "' `isSort`,'" + map.get("id").toString() + "' flowId,url from sys_interface_config where organization_id='" + orgId + "'" +
                    " and system_id='" + systemId + "' and purpose_id='" + purposeId + "'";
                List<Map<String, Object>> apiData = customizeMapper.queryList(apiSql);
                for (Map<String, Object> api : apiData) {
                    apis.add(api);
                }
            }
            //#endregion
            //生成接口日志
            Tuple2<Boolean, String> myResult = Tuple2.of(true, "");
            if (apis.size() < 1) {
                return myResult;
            }
            Boolean result = InsertInterfaceLog(apis, pactId, tableName, String.valueOf(root.get("code")), customParamJson, dbData, tableNode);

            if (result) {
                if (workTiming == 3 ||
                    workTiming == 4 ||
                    workTiming == 5) {
                    myResult = ExecuteAPI(pactId, tableName);
                } else {
                    ThreadPool.tenThreadExecutor.execute(() -> ExecuteAPI(pactId, tableName));
                }
                return myResult;
            } else {
                return Tuple2.of(false, "");
            }
        } catch (Exception ex) {
            log.error("分发异常:", ex);
            ErrorLog(pactId, tableName, "分发异常：" + ex.getMessage());
            return Tuple2.of(false, "分发异常：" + ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void setAPIInfo(Map<String, List<Map<String, Object>>> dbData, TableNode tableNode, Long pactId) {
        setAPIInfo(dbData, tableNode, pactId, 1, null);
    }

    // 删除原有成员变量声明

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Tuple2<Boolean, String> setAPIInfo(Map<String, List<Map<String, Object>>> dbData, TableNode tableNode, Long pactId, int workTiming) {
        return setAPIInfo(dbData, tableNode, pactId, workTiming, null);
    }

    /**
     * 重试（不重新生成接口参数）
     *
     * @param logId
     * @return
     */
    public R ReSend(Long logId) {
        try {
            customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(SysBusinessflowConfigMapper.class);
            String sqlStr = "select `status`,url,request_head,request_parm,model_id,pact_id,version from sys_interface_log where id='" + logId + "'";
            List<Map<String, Object>> logs = customizeMapper.queryList(sqlStr);
            if (logs.size() < 1) {
                return R.fail("重试失败：无效的日志id");
            }
            String status = logs.get(0).get("status").toString();
            if (!status.equals("3") && !status.equals("0")) {
                return R.fail("重试失败：执行失败的日志才能重发");
            }
            String url = logs.get(0).get("url").toString();
            String head = logs.get(0).get("request_head").toString();
            String parm = logs.get(0).get("request_parm").toString();
            Map<String, Object> headMap = new HashMap<>();
            if (!head.equals("")) {
                headMap = JSONObject.parseObject(head, Map.class);
            }
            LocalDateTime requestTime = LocalDateTime.now();
            String response = HttpHelper.BasePostRequest(parm, url, headMap);
            LocalDateTime responseTime = LocalDateTime.now();
            String pactCode = "";
            if (response.contains("code") && response.contains("data")) {
                JSONObject responseJson = JSONObject.parseObject(response);
                if (responseJson.get("code").toString().equals("1")) {
                    status = "2";
                    if (responseJson.getString("data") != null && !responseJson.getString("data").toString().equals("")) {
                        JSONArray dataJson = responseJson.getJSONArray("data");
                        for (int i = 0; i < dataJson.size(); i++) {
                            pactCode += ((JSONObject) dataJson.get(i)).get("pactCode");
                        }
                    }
                }
            }
            sqlStr = "update sys_interface_log set response_time='" + responseTime + "',`status`='" + status + "',response_msg='" + response.replace("'", "''") + "' ,response_pact_code='" + pactCode + "' where id='" + logId + "'";
            customizeMapper.execute(sqlStr);

            //更新单据同步状态
            if (status.equals("2")) {
                String modelTableName = logs.get(0).get("model_id").toString();
                Long pactId = Long.valueOf(logs.get(0).get("pact_id").toString());
                Integer version = Integer.valueOf(logs.get(0).get("version").toString());

                //批次是否都执行成功
                String countFailSql = "select count(*) from sys_interface_log where pact_id=" + pactId + " and " +
                    "model_id='" + modelTableName + "' and status<>2 and version =" + version;
                Long countFail = customizeMapper.count(countFailSql);
                if (countFail == 0) {
                    sqlStr = "update " + modelTableName + " set sync_status='1' where id='" + pactId + "'";
                    customizeMapper.execute(sqlStr);
                }
            }
            return R.ok();
        } catch (Exception ex) {
            return R.fail("重试失败：" + ex.getMessage());
        }
    }


    //#endregion

    /**
     * 写入接口日志
     *
     * @param apis
     * @param pactId
     * @param modelId
     * @return
     */
    private boolean InsertInterfaceLog(List<Map<String, Object>> apis,
                                       Long pactId,
                                       String modelId,
                                       String pactCode,
                                       JSONObject customParamJson,
                                       Map<String, List<Map<String, Object>>> pactData,
                                       TableNode tableNode) {
        try {
            //获取最新版本号
            String sqlStr = "select CONVERT(IFNULL(max(version),0),SIGNED) version from sys_interface_log where model_id='" + modelId + "' and pact_id='" + pactId + "'";
            List<Map<String, Object>> versionData = customizeMapper.queryList(sqlStr);
            int version = 1;
            if (versionData.size() > 0) {
                version = Integer.parseInt(versionData.get(0).get("version").toString()) + 1;
            }
            List<String> sqlCollection = new ArrayList<>();
            List<SysInterfaceLog> sysInterfaceLogs = new ArrayList<>();
            for (Map<String, Object> api : apis) {
                IsError.set(false);
                String pramJson = "";//参数
                int status = 0;//待执行
                JSONObject headJson = new JSONObject();
                //获取请求参数
                JSONObject parmJson;
                if (customParamJson != null) {
                    parmJson = customParamJson;
                } else {
                    parmJson = GetNodeJson(api.get("id").toString(), pactData, tableNode);
                }
                //获取请求头参数
                sqlStr = "select * from sys_interface_config_head where interface_id ='" + api.get("id") + "'";
                List<Map<String, Object>> headMaps = customizeMapper.queryList(sqlStr);
                if (headMaps != null) {
                    for (Map<String, Object> headMap : headMaps) {
                        headJson.put(headMap.get("item_name").toString(), headMap.get("item_value"));
                    }
                }
                if (IsError.get()) {
                    status = 3;//执行失败;
                }

                SysInterfaceLog sysInterfaceLog = new SysInterfaceLog();
                sysInterfaceLog.setId(AbstractDynamicsFormService.sequence.nextId());
                sysInterfaceLog.setInterfaceId(Long.valueOf(api.get("id").toString()));
                sysInterfaceLog.setVersion(version);
                sysInterfaceLog.setIndex(Integer.valueOf(api.get("index").toString()));
                sysInterfaceLog.setStatus(String.valueOf(status));
                sysInterfaceLog.setOrgId(api.get("organization_id").toString());
                sysInterfaceLog.setSystemId(api.get("system_id").toString());
                sysInterfaceLog.setPurposeId(api.get("purpose_id").toString());
                sysInterfaceLog.setUrl(api.get("url").toString());
                sysInterfaceLog.setRequestHead(headJson.toJSONString());
                sysInterfaceLog.setRequestParm(JsonUtils.toJsonString(parmJson));
                sysInterfaceLog.setIsSort(api.get("isSort").toString().equals("1"));
//                sysInterfaceLog.setIsSort(Boolean.parseBoolean(api.get("isSort").toString()));
                sysInterfaceLog.setPactId(pactId);
                sysInterfaceLog.setModelId(modelId);
                sysInterfaceLog.setCreateTime(LocalDateTime.now());
                sysInterfaceLog.setPactCode(pactCode);
                sysInterfaceLogs.add(sysInterfaceLog);
            }
            MybatisBatch<SysInterfaceLog> mybatisBatch = new MybatisBatch<SysInterfaceLog>(ApplicationContextHolder.getBean(SqlSessionFactory.class), sysInterfaceLogs);
            MybatisBatch.Method<SysInterfaceLog> method = new MybatisBatch.Method<>(InterfaceLogMapper.class);
            mybatisBatch.execute(method.insert());
        } catch (Exception ex) {
            ErrorLog(pactId, modelId, "生成日志异常：" + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
        return true;
    }

    /**
     * 获取接口的参数，json格式
     *
     * @param interfaceId
     * @return
     */
    private JSONObject GetNodeJson(String interfaceId, Map<String, List<Map<String, Object>>> pactData, TableNode tableNode) {
        JSONObject nodeJson = new JSONObject(new LinkedHashMap<>());
        try {
            //获取参数节点
            String sqlStr = "select * from sys_interface_config_node where interface_id ='" + interfaceId + "' order by sort_index ";
            List<Map<String, Object>> nodeData = customizeMapper.queryList(sqlStr);
            if (nodeData.size() < 0) {
                return nodeJson;
            }
            //获取根节点
            List<Map<String, Object>> rootMap = nodeData.stream().filter(map -> map.get("parent_node") == null || map.get("parent_node").equals("")).collect(Collectors.toList());
            for (Map<String, Object> item : rootMap) {
                //获取根节点下参数
                sqlStr = "select * from sys_interface_config_node_parm where node_id ='" + item.get("id") + "' order by sort_index ";
                List<Map<String, Object>> parms = customizeMapper.queryList(sqlStr);
                JSONObject parmJson = GetParmJson(parms, item.get("data_node").toString(), null, pactData);
                nodeJson = JsonMerge(nodeJson, parmJson);
            }
            //子节点
            for (Map<String, Object> item : rootMap) {
                String nodeName = item.get("node_name").toString();
                List<Map<String, Object>> childMap = nodeData.stream().filter(map -> map.get("parent_node") != null && map.get("parent_node").equals(nodeName)).collect(Collectors.toList());
                for (Map<String, Object> mode : childMap) {
                    if(Objects.equals(mode.get("data_type").toString(), "1")){
                        JSONObject childJson = ChildNode2Json(mode, nodeData, pactData, tableNode);
                        nodeJson.put(mode.get("node_name").toString(), childJson);
                    }else{
                        List<Map<String, Object>> dataRows = pactData.get(item.get("data_node").toString());
                        if (dataRows == null) {
                            continue;
                        }

                        JSONArray jsonArray = new JSONArray();
                        for(Map<String, Object> dataRow : dataRows){
                            JSONArray childJson = ChildNode2Json_Array(mode, nodeData, dataRow.get("id").toString(), pactData, tableNode);
                            jsonArray.addAll(childJson);
                        }

                        nodeJson.put(mode.get("node_name").toString(), jsonArray);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("参数异常", ex);
            nodeJson.put("参数异常", ex.getMessage());
            IsError.set(true);
        }
        return nodeJson;
    }

    /**
     * 递归转换子节点为json
     *
     * @param myNode
     * @param nodeData
     * @return
     */
    private JSONObject ChildNode2Json(Map<String, Object> myNode,
                                      List<Map<String, Object>> nodeData,
                                      Map<String, List<Map<String, Object>>> pactData,
                                      TableNode tableNode) {
        JSONObject nodeJson = new JSONObject(new LinkedHashMap<>());
        //获取节点下参数
        String sqlStr = "select * from sys_interface_config_node_parm where node_id ='" + myNode.get("id") + "' order by sort_index ";
        List<Map<String, Object>> parms = customizeMapper.queryList(sqlStr);
        JSONObject parmJson = GetParmJson(parms, myNode.get("data_node").toString(), null, pactData);
        nodeJson = JsonMerge(nodeJson, parmJson);
        //子节点的子节点
        String nodeName = myNode.get("node_name").toString();
        List<Map<String, Object>> childMap = nodeData.stream().filter(map -> map.get("parent_node") != null && map.get("parent_node").equals(nodeName)).collect(Collectors.toList());
        for (Map<String, Object> mode : childMap) {
            if (mode.get("data_type").equals("1")) {
                JSONObject childJson = ChildNode2Json(mode, nodeData, pactData, tableNode);
                nodeJson.put(mode.get("node_name").toString(), childJson);
            } else {
                //当前节点的主键
                String tableName = myNode.get("data_node").toString();
                List<Map<String, Object>> myTable = (List<Map<String, Object>>) pactData.get(tableName);
                String id = myTable.get(0).get("id").toString();
                JSONArray childJson = ChildNode2Json_Array(mode, nodeData, id, pactData, tableNode);
                nodeJson.put(mode.get("node_name").toString(), childJson);
            }
        }
        return nodeJson;
    }

    /**
     * 递归转换子节点为json数组
     *
     * @param myNode
     * @param nodeData
     * @return
     */
    private JSONArray ChildNode2Json_Array(Map<String, Object> myNode,
                                           List<Map<String, Object>> nodeData,
                                           String parentId,
                                           Map<String, List<Map<String, Object>>> pactData,
                                           TableNode tableNode) {
        JSONArray nodeJson = new JSONArray();
        String nodeTable = myNode.get("data_node").toString();
        List<Map<String, Object>> tableData;
        if (nodeTable.equals(tableNode.getTableName())) {
            tableData = pactData.get(nodeTable);
        } else {
            String foreignKey = TableNode.findChildNodes(tableNode).stream().filter(map -> map.getTableName().equals(nodeTable)).collect(Collectors.toList()).get(0).getForeignKey();
            String nForeignKey = NameRule(foreignKey);
            tableData = pactData.get(nodeTable).stream().filter(map -> map.get(nForeignKey).toString().equals(parentId)).collect(Collectors.toList());
        }
        for (Map<String, Object> dataRow : tableData) {
            JSONObject drJson = new JSONObject(new LinkedHashMap<>());
            //获取当前节点下参数
            String sqlStr = "select * from sys_interface_config_node_parm where node_id ='" + myNode.get("id") + "' order by sort_index ";
            List<Map<String, Object>> parms = customizeMapper.queryList(sqlStr);
            JSONObject parmJson = GetParmJson(parms, myNode.get("data_node").toString(), dataRow, pactData);
            drJson = JsonMerge(drJson, parmJson);
            //子节点的子节点
            String nodeName = myNode.get("node_name").toString();
            List<Map<String, Object>> childMap = nodeData.stream().filter(map -> map.get("parent_node") != null && map.get("parent_node").equals(nodeName)).collect(Collectors.toList());
            for (Map<String, Object> mode : childMap) {
                if (mode.get("data_type").equals("1")) {
                    JSONObject childJson = ChildNode2Json(mode, nodeData, pactData, tableNode);
                    drJson.put(mode.get("node_name").toString(), childJson);
                } else {
                    JSONArray childJson = ChildNode2Json_Array(mode, nodeData, dataRow.get("id").toString(), pactData, tableNode);
                    drJson.put(mode.get("node_name").toString(), childJson);
                }
            }
            nodeJson.add(drJson);
        }
        return nodeJson;
    }

    /**
     * 把参数转换成json对象
     *
     * @param parms     参数集合
     * @param nodeTable 节点表名
     * @return
     */
    private JSONObject GetParmJson(List<Map<String, Object>> parms,
                                   String nodeTable,
                                   Map<String, Object> dataRow,
                                   Map<String, List<Map<String, Object>>> pactData) {
        JSONObject parmJson = new JSONObject(new LinkedHashMap<>());

        for (Map<String, Object> parmMap : parms) {
            String value = "";
            String parmKey = parmMap.get("parm_key").toString();
            try {
                if (parmMap.get("data_fun") != null) {
                    String fun = parmMap.get("data_fun").toString();
                    if (fun.indexOf("[") >= 0 && fun.indexOf("]") > 0 && fun.indexOf("[") < fun.indexOf("]")) {
                        String left = "@#$%&*";//代替[
                        String right = "*&%$#@";//代替]
                        while (fun.indexOf("[") >= 0 && fun.indexOf("]") > 0 && fun.indexOf("[") < fun.indexOf("]")) {
                            int begin = fun.indexOf("[");
                            int end = fun.indexOf("]");
                            String nodeCol = fun.substring(begin + 1, end);
                            String tableName = "";
                            String colName = "";
                            if (nodeCol.contains(".")) {
                                //需要从关联表取数据
                                String[] array = nodeCol.split("\\.");
                                if (array.length > 1) {
                                    tableName = array[0];
                                    colName = array[1];
                                    List<Map<String, Object>> dt = (List<Map<String, Object>>) pactData.get(tableName);
                                    if (dt != null && dt.size() > 0) {
                                        value = serialize(dt.get(0).get(colName));
                                    }
                                }
                            } else {
                                //从当前表取数
                                colName = nodeCol;
                                if (dataRow != null) {
                                    if (dataRow.get(colName) != null) {
                                        value = serialize(dataRow.get(colName));
                                    }
                                } else {
                                    List<Map<String, Object>> dt = (List<Map<String, Object>>) pactData.get(nodeTable);
                                    if (dt != null && dt.size() > 0) {
                                        value = serialize(dt.get(0).get(colName));
                                    }
                                }
                            }
                            value = value.replace("[", left).replace("]", right);
                            fun = fun.replace("[" + nodeCol + "]", "'" + value.replace("\'", "\\\'") + "'");
                        }

                        fun = fun.replace(left, "[").replace(right, "]");
                        String sqlstr = "select (" + fun + ") as '" + parmKey + "'";
                        List<Map<String, Object>> dtResult = customizeMapper.queryList(sqlstr);
                        if (dtResult != null && dtResult.size() > 0 && dtResult.get(0) != null) {
                            value = dtResult.get(0).get(parmKey).toString();
                        } else {
                            value = "";
                        }
                    } else {
                        //固定值
                        value = fun;
                    }
                } else {
                    //赋默认值,int,decimal,boolean
                    if (parmMap.get("data_type").equals(DataType.int_d.ordinal())) {
                        value = "0";
                    } else if (parmMap.get("data_type").equals(DataType.decimal_d.ordinal())) {
                        value = "0.00";
                    } else if (parmMap.get("data_type").equals(DataType.boolean_d.ordinal())) {
                        value = "false";
                    }
                }
                parmJson.put(parmKey, ChangeDataType(value, (int) parmMap.get("data_type")));
            } catch (Exception ex) {
                log.error("配置异常，无法取到值", ex);
                parmJson.put(parmKey, "配置异常，无法取到值" + ex.getMessage());
                IsError.set(true);
            }
        }
        return parmJson;
    }

    /**
     * 执行调用接口
     *
     * @param pactId
     * @param modelId
     */
    @SneakyThrows
    private Tuple2<Boolean, String> ExecuteAPI(Long pactId, String modelId) {
        int IsSuccess = 1;
        String errorMsg = "";
        //region 最新版本且状态为待执行，无需顺序执行
        String sqlStr = "select * from sys_interface_log where status=0 and is_sort=0 and version=(select max(version) from sys_interface_log where  pact_id='" + pactId + "' and model_id ='" + modelId + "') and model_id='" + modelId + "' and pact_id='" + pactId + "'";
        List<Map<String, Object>> logData = customizeMapper.queryList(sqlStr);
        for (Map<String, Object> log : logData) {
            try {
                String headInfo = log.get("request_head").toString();
                Map<String, Object> headMap = new HashMap<>();
                if (!headInfo.equals("")) {
                    headMap = JSONObject.parseObject(headInfo, Map.class);
                }
                LocalDateTime requestTime = LocalDateTime.now();
                int status = 3;
                sqlStr = "update sys_interface_log set request_time='" + requestTime + "',status='1' where id='" + log.get("id") + "'";
                customizeMapper.execute(sqlStr);
                //调接口
                String response = HttpHelper.BasePostRequest(log.get("request_parm").toString(), log.get("url").toString(), headMap);
                LocalDateTime responseTime = LocalDateTime.now();
                String pactCode = "";
                if (response.contains("code") && response.contains("data")) {
                    try {
                        JSONObject responseJson = JSONObject.parseObject(response);
                        if (responseJson.get("code").toString().equals("1")) {
                            status = 2;//成功
                        } else {
                            IsSuccess = 0;
                            errorMsg += response + "\r\n";
                        }
                    } catch (Exception ex) {
                        IsSuccess = 0;
                        errorMsg += response + "\r\n";
                    }
                }
                //记录结果
                sqlStr = "update sys_interface_log set response_time='" + responseTime + "',status='" + status + "',response_msg='" + response.replace("'", "''") + "' ,response_pact_code='" + pactCode + "' where id='" + log.get("id") + "'";
                customizeMapper.queryList(sqlStr);
                //接口执行失败，不再执行后续接口
                if (status == 3) {
                    break;
                }
            } catch (Exception ex) {
                errorMsg += ex.getMessage() + "\r\n";
                IsSuccess = 0;
                sqlStr = "update sys_interface_log set status='3',response_msg='" + ex.getMessage().replace("'", "''") + "' where id='" + log.get("id") + "'";
                customizeMapper.queryList(sqlStr);
            }
        }
        //endregion
        //region 最新版本，状态为待执行，要顺序执行
        sqlStr = "select * from sys_interface_log where status=0 and is_sort=1 and version=(select max(version) from sys_interface_log where pact_id='"
            + pactId + "' and model_id ='" + modelId + "') and model_id='" + modelId + "' and pact_id='" + pactId + "' order by `index` ";
        List<Map<String, Object>> logDataSort = customizeMapper.queryList(sqlStr);
        // 回传单号
        String pactCode = "";
        ArrayList<String> pactCodeList = new ArrayList<>();
        for (Map<String, Object> logItem : logDataSort) {
            try {
                String headInfo = logItem.get("request_head").toString();
                Map<String, Object> headMap = new HashMap<>();
                if (!headInfo.equals("")) {
                    headMap = JSONObject.parseObject(headInfo, Map.class);
                }
                LocalDateTime requestTime = LocalDateTime.now();
                int status = 3;
                sqlStr = "update sys_interface_log set request_time='" + requestTime + "',status='1' where id='" + logItem.get("id") + "'";
                customizeMapper.execute(sqlStr);
                //调接口
                String response = HttpHelper.BasePostRequest(logItem.get("request_parm").toString(), logItem.get("url").toString(), headMap);
                LocalDateTime responseTime = LocalDateTime.now();
                if (response.contains("code") && response.contains("data")) {
                    try {
                        JSONObject responseJson = JSONObject.parseObject(response);
                        if (responseJson.get("code").toString().equals("1")) {
                            status = 2;//成功
                            JSONArray dataJson = responseJson.getJSONArray("data");
                            if (dataJson != null) {
                                for (int i = 0; i < dataJson.size(); i++) {
                                    pactCode = (String) ((JSONObject) dataJson.get(i)).get("pactCode");
                                    pactCodeList.add(pactCode);
                                }
                            }
                        } else {
                            pactCode = "";
                            IsSuccess = 0;
                            errorMsg += response + "\r\n";
                        }
                    } catch (Exception ex) {
                        pactCode = "";
                        IsSuccess = 0;
                        errorMsg += response + "\r\n";
                    }
                }
                //记录结果
                sqlStr = "update sys_interface_log set Response_Time='" + responseTime + "',status='" + status + "',response_msg='" + response.replace("'", "''") + "' ,response_pact_code='" + pactCode + "' where id='" + logItem.get("id") + "'";
                customizeMapper.queryList(sqlStr);
                //接口执行失败，不再执行后续接口
                if (status == 3) {
                    break;
                }
            } catch (Exception ex) {
                log.error("执行分发异常", ex);
                IsSuccess = 0;
                errorMsg += ex.getMessage() + "\r\n";
                sqlStr = "update sys_interface_log set status='3',response_msg='" + ex.getMessage().replace("'", "''") + "' where id='" + logItem.get("id") + "'";
                customizeMapper.queryList(sqlStr);
                break;
            }
        }

        //endregion
        final int IsSuccessFinal = IsSuccess;
        String finalPactCode;
        if (pactCodeList.isEmpty()) {
            finalPactCode = "";
        } else {
            finalPactCode = String.join(",", pactCodeList);
        }

        ThreadPool.fiftenThreadExecutor.execute(() -> {
            try {
                Thread.sleep(1000);

                if (!finalPactCode.isEmpty()) {
                    String sql = "update " + modelId + " set response_pact_code='" + finalPactCode + "',sync_status=" + IsSuccessFinal + " where id='" + pactId + "'";
                    customizeMapper.execute(sql);
                }

                //没有回传单号，才更新同步状态
                String sqlForCodeIsNull = "update " + modelId + " set sync_status=" + IsSuccessFinal + " where id='" + pactId + "' and response_pact_code is null ";
                customizeMapper.execute(sqlForCodeIsNull);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        if (IsSuccess == 1) {
            return Tuple2.of(true, "");
        } else {
            return Tuple2.of(false, errorMsg);
        }
    }


    //region 公用

    /**
     * 数字序列化，防止科学计数法
     *
     * @param value
     * @return
     * @throws IOException
     */
    private static String serialize(Object value) throws IOException {
        if (Objects.nonNull(value)) {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).stripTrailingZeros().toPlainString();
            } else {
                return value.toString();
            }
        } else {
            return "";
        }
    }

    /**
     * 合并json
     *
     * @param main
     * @param part
     * @return
     */
    private JSONObject JsonMerge(JSONObject main, JSONObject part) {
        for (String key : part.keySet()) {
            main.put(key, part.get(key));
        }
        return main;
    }

    /**
     * 错误日志
     *
     * @param pactId
     * @param modelId
     * @param errorMsg
     */
    private void ErrorLog(Long pactId, String modelId, String errorMsg) {
        //获取最新版本号
        String sqlStr = "select CONVERT(IFNULL(max(version),0),SIGNED) version from sys_interface_log where model_id='" + modelId + "' and pact_id='" + pactId + "'";
        List<Map<String, Object>> versionData = customizeMapper.queryList(sqlStr);
        int version = 1;
        if (versionData.size() > 0) {
            version = Integer.parseInt(versionData.get(0).get("version").toString()) + 1;
        }

        long id = AbstractDynamicsFormService.sequence.nextId();

        sqlStr = "insert into sys_interface_log (id,version,`index`,`status`,request_parm,pact_id,model_id,create_time)\n" +
            "values (" + id + ",'" + version + "','1','3','" + errorMsg.replace("'", "''") + "','" + pactId
            + "','" + modelId + "','" + LocalDateTime.now() + "')";
        customizeMapper.execute(sqlStr);
    }

    /**
     * 转换数据类型
     *
     * @param value
     * @param dataType
     * @return
     */
    private Object ChangeDataType(String value, int dataType) {
        try {
            switch (DataType.values()[dataType]) {
                case int_d:
                    if (value.equals("")) {
                        return 0;
                    }
                    return Integer.parseInt(value);
                case decimal_d:
                    if (value.equals("")) {
                        return 0.00;
                    }
                    if (value.contains("0.0000")) {
                        return value;
                    }
                    return new BigDecimal(value);
                case boolean_d:
                    return value.equals("1");
                default:
                    return value;
            }
        } catch (Exception ex) {
            return value;
        }
    }

    private String NameRule(String name) {
        if (name.contains("_")) {
            String[] array = name.split("_");
            String out = array[0];
            for (int i = 1; i < array.length; i++) {
                char[] chars = array[i].toCharArray();
                chars[0] = toUpperCase(chars[0]);
                out = out + String.valueOf(chars);
            }
            return out;
        } else {
            return name;
        }
    }

    public static char toUpperCase(char c) {
        if (97 <= c && c <= 122) {
            c ^= 32;
        }
        return c;
    }
    //endregion
}
