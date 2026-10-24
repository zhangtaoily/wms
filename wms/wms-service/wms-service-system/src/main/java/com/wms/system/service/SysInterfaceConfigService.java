package com.wms.system.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.system.*;
import com.wms.domain.view.InterfaceConfigDataView;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.utils.TableNodeHelper;
import com.wms.system.mapper.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class SysInterfaceConfigService extends AbstractDynamicsFormService<SysInterfaceConfig, SysInterfaceConfigMapper> {

    @Resource
    private SysInterfaceConfigMapper sysInterfaceConfigMapper;

    @Override
    public SysInterfaceConfigMapper supplierMapper() {
        return sysInterfaceConfigMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysInterfaceConfig.class);
        root.setMapper(SysInterfaceConfigMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysInterfaceConfig.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysInterfaceConfig.class).getKeyColumn());
        root.setJoinColumns("sys_dict_data.dict_label orgName," +
            TableNodeHelper.getCommonJoinColumnsNotAudit());
        root.setJoinTables("left join sys_dict_data on sys_dict_data.dict_value= sys_interface_config.organization_id and sys_dict_data.dict_type='interface_org'" +
            TableNodeHelper.getCommonJoinTablesNotAudit(root.getTableName()));

        //子表请求头
        TableNode head = new TableNode();
        head.setTableClass(SysInterfaceConfigHead.class);
        head.setParentTable(TableInfoHelper.getTableInfo(SysInterfaceConfig.class).getTableName());
        head.setMapper(SysInterfaceConfigHeadMapper.class);
        head.setForeignKey("interface_id");
        head.setTableName(TableInfoHelper.getTableInfo(SysInterfaceConfigHead.class).getTableName());
        head.setPrimaryKey(TableInfoHelper.getTableInfo(SysInterfaceConfigHead.class).getKeyColumn());

        //子表请求参数
        TableNode node = new TableNode();
        node.setTableClass(SysInterfaceConfigNode.class);
        node.setParentTable(TableInfoHelper.getTableInfo(SysInterfaceConfig.class).getTableName());
        node.setMapper(SysInterfaceConfigNodeMapper.class);
        node.setForeignKey("interface_id");
        node.setTableName(TableInfoHelper.getTableInfo(SysInterfaceConfigNode.class).getTableName());
        node.setPrimaryKey(TableInfoHelper.getTableInfo(SysInterfaceConfigNode.class).getKeyColumn());
        node.setOrderBy("order by sort_index");
        //主表添加子表
        root.setChildren(ListUtils.of(head, node));

        //孙表
        TableNode parm = new TableNode();
        parm.setParentTable(TableInfoHelper.getTableInfo(SysInterfaceConfigNode.class).getTableName());
        parm.setTableClass(SysInterfaceConfigNodeParm.class);
        parm.setMapper(SysInterfaceConfigNodeParmMapper.class);
        parm.setForeignKey("node_id");
        parm.setTableName(TableInfoHelper.getTableInfo(SysInterfaceConfigNodeParm.class).getTableName());
        parm.setPrimaryKey(TableInfoHelper.getTableInfo(SysInterfaceConfigNodeParm.class).getKeyColumn());
        parm.setOrderBy("order by sort_index");
        //子表添加孙表
        node.setChildren(ListUtils.of(parm));
        return root;
    }

    ///对应模块
    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_INTERFACE_CFG;
    }

    ///分页查询
    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        InterfaceConfigDataView configDataView = new InterfaceConfigDataView();
        return super.queryTablePageList(configDataView.fetchDataView(), pageQuery);
    }

    @Override
    public Tuple2<Integer, String> checkSave(BasicBO bo,
                                             Map<String, List<Map<String, Object>>> oldTableEntityMap,
                                             Map<String, List<Map<String, Object>>> newTableEntityMap) {
        //校验系统，组织，用途是否重复
        Map<String, List<Object>> tableEntityMap = getTableEntityMap(bo);
        String mainTable = TableInfoHelper.getTableInfo(SysInterfaceConfig.class).getTableName();
        List<Object> configList = tableEntityMap.get(mainTable);
        SysInterfaceConfig model = (SysInterfaceConfig) configList.get(0);
        String sqlStr = "select 1 from sys_interface_config where organization_id='" + model.getOrganizationId()
            + "' and system_id='" + model.getSystemId() + "' and purpose_id='" + model.getPurposeId() + "'";
        if (!bo.isAdd()) {
            sqlStr += " and id<>'" + model.getId() + "'";
        }
        List<Map<String, Object>> data = sysInterfaceConfigMapper.queryList(sqlStr);
        if (data.size() > 0 && data.get(0) != null && !Objects.equals(model.getPurposeId(), "72")) {
            return Tuple2.of(0, "已经存在相同系统，组织，用途的接口，请勿重复创建！");
        }

        return Tuple2.of(1, null);
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        try {
            Map<String, List<Map<String, Object>>> data = doLoad(id);
            Map<String, Object> root = data.get("sys_interface_config").get(0);
            String orgId = root.get("organizationId").toString();
            String sysId = root.get("systemId").toString();
            String purId = root.get("purposeId").toString();
            String sqlStr = "select sys_dict_data.dict_label modelName from sys_businessflow_config_detail\n" +
                "left join sys_businessflow_config on sys_businessflow_config.id=sys_businessflow_config_detail.business_id\n" +
                "left join sys_dict_data on sys_dict_data.dict_value=sys_businessflow_config.model_id and sys_dict_data.dict_type='model_type'\n" +
                "where system_id='" + sysId + "' and org_id='" + orgId + "' and purpose_id='" + purId + "'";
            List<Map<String, Object>> model = sysInterfaceConfigMapper.queryList(sqlStr);
            if (model.size() > 0 && model.get(0) != null) {
                return Tuple2.of(false, "该接口已被模块" + model.get(0).get("modelName").toString() + "使用，若要删除，请先修改该模块的接口业务流配置！");
            }
        } catch (Exception ex) {
            return Tuple2.of(false, "checkDelete异常：" + ex.toString());
        }
        return super.checkDelete(id);
    }

    private Map<String, List<Map<String, Object>>> data;//单据数据集

    //region  参数预览
    public JSONObject PreviewParm(Long configId) {
        JSONObject jsonPram = new JSONObject();
        try {
            data = doLoad(configId);
            List<Map<String, Object>> nodes = data.get("sys_interface_config_node");//节点表
            List<Map<String, Object>> parms = data.get("sys_interface_config_node_parm");//参数表
            //根节点
            List<Map<String, Object>> rootMap = nodes.stream().filter(map -> map.get("parentNode") == null || map.get("parentNode").equals("")).collect(Collectors.toList());
            for (Map<String, Object> item : rootMap) {
                String nodeId = item.get("id").toString();
                //获取节点下参数
                List<Map<String, Object>> myParms = parms.stream().filter(map -> map.get("nodeId") != null && map.get("nodeId").equals(nodeId)).collect(Collectors.toList());
                for (Map<String, Object> map : myParms) {
                    jsonPram.put(map.get("parmKey").toString(), "");
                }
            }
            //子节点
            for (Map<String, Object> item : rootMap) {
                List<Map<String, Object>> childNodes = nodes.stream().filter(map -> map.get("parentNode") != null && map.get("parentNode").equals(item.get("nodeName"))).collect(Collectors.toList());
                jsonPram = GetChildNodePramJson_NoData(childNodes, jsonPram);
            }
        } catch (Exception ex) {
            String m = ex.getMessage();
        }
        return jsonPram;
    }


    /**
     * 将子节点参数组合成json，不赋值
     *
     * @param childNodes 节点集合
     * @param jsonResult
     * @return
     */
    public JSONObject GetChildNodePramJson_NoData(List<Map<String, Object>> childNodes, JSONObject jsonResult) {
        List<Map<String, Object>> parms = data.get("sys_interface_config_node_parm");//参数表
        for (Map<String, Object> item : childNodes) {
            JSONObject nodeJson = new JSONObject();
            String nodeId = item.get("id").toString();
            //获取节点下参数
            List<Map<String, Object>> myParms = parms.stream().filter(map -> map.get("nodeId") != null && map.get("nodeId").equals(nodeId)).collect(Collectors.toList());
            for (Map<String, Object> map : myParms) {
                nodeJson.put(map.get("parmKey").toString(), "");
            }

            List<Map<String, Object>> nodes = data.get("sys_interface_config_node");//节点表
            //获取下级节点
            List<Map<String, Object>> myNodes = nodes.stream().filter(map -> map.get("parentNode") != null && map.get("parentNode").equals(item.get("nodeName"))).collect(Collectors.toList());
            nodeJson = GetChildNodePramJson_NoData(myNodes, nodeJson);
            //判断该节点是否数组
            if (item.get("dataType").equals("2")) {
                //数组
                JSONArray nodeArray = new JSONArray();
                nodeArray.add(nodeJson);
                jsonResult.put(item.get("nodeName").toString(), nodeArray);
            } else {
                //单个对象
                jsonResult.put(item.get("nodeName").toString(), nodeJson);
            }
        }
        return jsonResult;
    }
    //endregion

}
