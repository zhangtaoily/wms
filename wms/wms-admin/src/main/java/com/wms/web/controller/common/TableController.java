package com.wms.web.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.wms.common.convert.ExcelBigNumberConvert;
import com.baomidou.lock.annotation.Lock4j;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FieldTypeEnum;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.rule.Operator;
import com.wms.common.utils.DateUtils;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.vo.PushDownVo;
import com.wms.domain.vo.ExportVo;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.Lock;
import com.wms.service.common.spi.CommonFormManager;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用列表接口
 */
@Validated
@RestController
@RequestMapping("/common/table/")
public class TableController {

    /**
     * 通用列表分页查询
     */
    @SaIgnore()
    @PostMapping("/list")
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> list(@RequestBody PageQuery pageQuery) {
        return CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(pageQuery.getFunctionName())).queryPageList(pageQuery);
    }


    /**
     * 通用列表删除
     */
    @SaIgnore()
    @DeleteMapping("/{function}/{id}")
    public R delete(@PathVariable("function") String function,
                    @PathVariable("id") Long[] id) {
        //Tuple2<Boolean, String> result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).remove(id);
        Tuple2<Boolean, String> result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchRemove(id);
        if (result.getFrist()) {
            return R.ok(result.getSecond());
        } else {
            return R.fail(result.getSecond());
        }
    }

    /**
     * 保存
     */
    @SaIgnore()
    @PostMapping("/{function}")
    public R save(@PathVariable("function") String function, @RequestBody() BasicBO bo) {
        synchronized (Lock.OTHER_LOCK) {
            return doSave(function, bo);
        }
    }

    public R doSave(String function, BasicBO bo) {
        if (ListUtils.isNotEmpty(bo.getDatas())) {
            Tuple2<Boolean, Object> batchResult = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchSave(bo);
            if (batchResult.getFrist()) {
                return R.ok(batchResult.getSecond());
            } else {
                return R.fail(String.valueOf(batchResult.getSecond()));
            }
        } else {
            Tuple2<Integer, Object> result;
            result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).save(bo);
            if (result.getFrist() == 0) {
                return R.fail(String.valueOf(result.getSecond()));
            }
            if (result.getFrist() == 1) {
                return R.ok(result.getSecond());
            }
            if (result.getFrist() == 2) {
                return R.confirm(String.valueOf(result.getSecond()));
            }
        }
        return R.fail("错误的请求");
    }

    /**
     * 暂存
     */
    @SaIgnore()
    @PostMapping("/staging/{function}")
    public R staging(@PathVariable("function") String function, @RequestBody() BasicBO bo) {
        Tuple2<Boolean, Object> result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).staging(bo);
        if (result.getFrist()) {
            return R.ok(result.getSecond());
        } else {
            return R.fail(result.getSecond());
        }
    }

    /**
     * 单条记录获取
     */
    @SaIgnore()
    @GetMapping("/{function}/{id}")
    public R init(@PathVariable("function") String function, @PathVariable("id") Long id) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).init(id));
    }

    /**
     * 批量记录获取
     */
    @SaIgnore()
    @PostMapping("/{function}/batch/init")
    public R batchInit(@PathVariable("function") String function, @RequestBody List<String> ids) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchInit(ids));
    }

    /**
     * 复制
     */
    @SaIgnore()
    @PostMapping("/copy/{function}/{id}")
    public R copy(@PathVariable("function") String function, @PathVariable("id") Long id, @RequestBody(required = false) BasicBO bo) {
        Tuple2<Boolean, Object> result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).copy(id, bo);
        if (result.getFrist()) {
            return R.ok(result.getSecond());
        } else {
            return R.fail(result.getSecond());
        }
    }

    /**
     * 复制  关联关系也一起复制
     */
    @SaIgnore()
    @PostMapping("/copyAll/{function}/{id}")
    public R copyAll(@PathVariable("function") String function, @PathVariable("id") Long id, @RequestBody(required = false) BasicBO bo) {
        Tuple2<Boolean, Object> result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).copyAll(id, bo);
        if (result.getFrist()) {
            return R.ok(result.getSecond());
        } else {
            return R.fail(result.getSecond());
        }
    }

    /**
     * 初始化绑定
     */
    @SaIgnore()
    @GetMapping("/init/bind/{function}")
    public R initBind(@PathVariable("function") String function) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).initBind());
    }

    /**
     * 下推
     */
    @SaIgnore()
    @PostMapping("/push/{function}/{targetFunction}")
    public R pushDown(@PathVariable("function") String function, @PathVariable("targetFunction") String targetFunction, @RequestBody() List<PushDownVo> pushDowns) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).push(targetFunction, pushDowns));
    }

    /**
     * 提交
     */
    @SaIgnore()
    @Lock4j(keys = {"#id"})
    @PatchMapping("/submit/{function}/{id}")
    public R submit(@PathVariable("function") String function, @PathVariable("id") Long id, @RequestBody BasicBO bo) {
        return doSubmit(function,id, bo);
    }

    public R doSubmit(String function,Long id, BasicBO bo) {
        Tuple2<Integer, String> tuple2 = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).submit(id, bo);
        if (tuple2.getFrist() == 0) {
            return R.fail(String.valueOf(tuple2.getSecond()));
        }
        if (tuple2.getFrist() == 1) {
            return R.ok(tuple2.getSecond());
        }
        if (tuple2.getFrist() == 2) {
            return R.confirm(String.valueOf(tuple2.getSecond()));
        }

        return R.fail(tuple2.getSecond());
    }

    /**
     * 撤回
     */
    @SaIgnore()
    @PatchMapping("/roll/back/{function}/{id}")
    public R rollBack(@PathVariable("function") String function, @PathVariable("id") Long id) {
        Tuple2<Boolean, String> result = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).rollBack(id);
        if (result.getFrist()) {
            return R.ok(result.getSecond());
        }

        return R.fail(result.getSecond());
    }

    @SaIgnore()
    @PatchMapping("/reSend/{function}/{id}")
    public R reSend(@PathVariable("function") String function, @PathVariable("id") Long id) {
        return CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).reTry(id);
    }

    /**
     * 获取明细,没有表头
     */
    @SaIgnore()
    @PostMapping("/details/{function}")
    public R details(@PathVariable("function") String function, @RequestBody() BasicBO bo) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).getDetailList(bo));
    }

    /**
     * 获取明细,有表头
     */
    @SaIgnore()
    @PostMapping("/details/with/head/{function}")
    public R detailsWithHead(@PathVariable("function") String function, @RequestBody() BasicBO bo) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).getDetailListWithHead(bo));
    }

    /**
     * 检查
     */
    @SaIgnore()
    @PostMapping("/check/{function}/{isCheck}")
    public R check(@PathVariable("function") String function, @PathVariable("isCheck") Integer isCheck, @RequestBody List<Long> ids) {
        FunctionEnum functionEnum = FunctionEnum.valueOf(function);
        Tuple2<Boolean, String> result;
        if (1 == isCheck) {
            result = CommonFormManager.getInstance().getFormService(functionEnum).check(ids);
        } else if (0 == isCheck) {
            result = CommonFormManager.getInstance().getFormService(functionEnum).reCheck(ids);
        } else {
            throw new RuntimeException("isCheck参数错误");
        }
        if (result.getFrist()) {
            return R.ok(result.getSecond());
        }

        return R.fail(result.getSecond());
    }

    /**
     * 构建查询条件
     */
    private void buildQueryConditions(PageQuery query, ExportVo exportVo) {
        if("0".equals(exportVo.getIsAll())){
            PageQuery.ConditionItem conditionItem = new PageQuery.ConditionItem();
            String idsString = exportVo.getIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            conditionItem.setValue(idsString);
            conditionItem.setField("id");
            conditionItem.setOperate(Operator.IN);
            conditionItem.setLogicalOperator(Operator.AND);
            conditionItem.setFilterGroup(PageQuery.FilterGroup.FAST);
            conditionItem.setFieldType(FieldTypeEnum.TEXT);
            query.setConditions(Collections.singletonList(conditionItem));
        } else {
            if(!exportVo.getConditions().isEmpty()){
                query.setConditions(exportVo.getConditions());
            }
        }
    }

    /**
     * 检查导出数据量是否超过限制
     */
    private void checkExportLimit(FunctionEnum functionEnum, ExportVo exportVo) {
        PageQuery countQuery = new PageQuery();
        countQuery.setFunctionName(functionEnum.name());
        countQuery.setPageNum(1);
        countQuery.setPageSize(1); // 只获取总数，不获取实际数据

        buildQueryConditions(countQuery, exportVo);

        DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> countDataInfo =
            CommonFormManager.getInstance().getFormService(functionEnum).queryPageList(countQuery);

        if(countDataInfo.isMoreThanExportLimit()){
            throw new RuntimeException("超过导出条数50000的限制，请联系信息科导出！");
        }
    }

    /**
     * 获取表头信息
     */
    private List<SysFieldConfig> getHeaderInfo(FunctionEnum functionEnum) {
        PageQuery headerQuery = new PageQuery();
        headerQuery.setFunctionName(functionEnum.name());
        headerQuery.setPageNum(1);
        headerQuery.setPageSize(1); // 只取一条记录用于获取表头

        DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> headerDataInfo =
            CommonFormManager.getInstance().getFormService(functionEnum).queryPageList(headerQuery);

        return headerDataInfo.getHeaders();
    }

    /**
     * 构建Excel表头
     */
    private Tuple2<List<List<String>>, List<String>> buildExcelHeaders(List<SysFieldConfig> headersList) {
        List<List<String>> head = new ArrayList<>();
        List<String> headerFieldList = new ArrayList<>();

        for (SysFieldConfig config : headersList) {
            if (config.getColumnWidth() != null && Integer.parseInt(config.getColumnWidth()) > 0 && config.getIsShow() != 0) {
                head.add(Collections.singletonList(config.getHeaderText()));
                headerFieldList.add(config.getColumnName());
            }
        }

        return new Tuple2<>(head, headerFieldList);
    }

    /**
     * 转换数据为EasyExcel需要的格式
     */
    private List<List<Object>> convertDataToExcelFormat(List<TreeMap<String, Object>> rows, List<String> headerFieldList, List<SysFieldConfig> headersList) {
        List<List<Object>> dataList = new ArrayList<>();

        // 创建字段名到字段配置的映射
        Map<String, SysFieldConfig> fieldConfigMap = new HashMap<>();
        for (SysFieldConfig config : headersList) {
            if (config.getColumnWidth() != null && Integer.parseInt(config.getColumnWidth()) > 0 && config.getIsShow() != 0) {
                fieldConfigMap.put(config.getColumnName(), config);
            }
        }

        for (TreeMap<String, Object> row : rows) {
            List<Object> rowData = new ArrayList<>();
            for (String field : headerFieldList) {
                Object value = row.get(field);

                // 根据字段类型转换数据
                if (value != null) {
                    SysFieldConfig config = fieldConfigMap.get(field);
                    if (config != null && config.getFieldType() != null) {
                        value = convertValueByFieldType(value, config.getFieldType());
                    }
                }

                rowData.add(value);
            }
            dataList.add(rowData);
        }
        return dataList;
    }

    /**
     * 根据字段类型转换值
     */
    private Object convertValueByFieldType(Object value, String fieldType) {
        if (value == null) {
            return null;
        }

        // 如果已经是Date类型，格式化为字符串返回，避免EasyExcel动态写入找不到Date的Converter
        if (value instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.util.Date) value);
        }

        String valueStr = value.toString();
        if (valueStr.isEmpty()) {
            return value;
        }

        try {
            // 处理数字类型字段
            if (FieldTypeEnum.NUMBER.name().equals(fieldType) ||
                FieldTypeEnum.NUMBER_RANGE.name().equals(fieldType)) {
                // 尝试转换为BigDecimal，处理金额和精度
                return new BigDecimal(valueStr);
            }

            // 处理日期类型字段
            if (FieldTypeEnum.DATE.name().equals(fieldType) ||
                FieldTypeEnum.DATE_RANGE.name().equals(fieldType)) {
                // 直接返回字符串，避免EasyExcel动态写入时找不到Date的Converter
                return valueStr;
            }

            // 其他类型保持原样
            return value;
        } catch (Exception e) {
            // 转换失败时返回原值
            return value;
        }
    }

    /**
     * 设置HTTP响应头
     */
    private void setupResponseHeaders(HttpServletResponse response, String excelName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode(excelName + ".xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("Cache-Control", "no-cache");
    }

    @PostMapping("/export/{function}")
    public void exportExcel(HttpServletResponse response, @PathVariable("function") String function, @RequestBody ExportVo exportVo) throws IOException {
        FunctionEnum functionEnum = FunctionEnum.valueOf(function);

        // 先进行数据量检查，避免设置响应头后抛出异常导致空白文件
        checkExportLimit(functionEnum, exportVo);

        // 使用ExcelUtil的标准导出方法，避免手动处理响应头
        String excelName = functionEnum.getDesc() + DateUtils.getTime();
        setupResponseHeaders(response, excelName);

        ServletOutputStream outputStream = null;
        com.alibaba.excel.ExcelWriter excelWriter = null;

        try {
            outputStream = response.getOutputStream();

            // 创建EasyExcel的ExcelWriter，直接传入输出流进行流式写入
            excelWriter = EasyExcel.write(outputStream)
                .autoCloseStream(false)
                // 自动适配
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                // 大数值自动转换 防止失真
                .registerConverter(new ExcelBigNumberConvert())
                .build();

            // 获取表头信息
            List<SysFieldConfig> headersList = getHeaderInfo(functionEnum);
            Tuple2<List<List<String>>, List<String>> headerInfo = buildExcelHeaders(headersList);
            List<List<String>> head = headerInfo.getFrist();
            List<String> headerFieldList = headerInfo.getSecond();

            // 创建WriteSheet，设置表头
            WriteSheet writeSheet = EasyExcel.writerSheet()
                .head(head)
                .build();

            // 分页导出数据，避免一次性加载所有数据
            int pageSize = 50000; // 每页处理1000条记录
            int pageNum = 1;
            boolean hasMoreData = true;

            while (hasMoreData) {
                PageQuery pageQuery = new PageQuery();
                pageQuery.setFunctionName(function);
                pageQuery.setPageNum(pageNum);
                pageQuery.setPageSize(pageSize);

                buildQueryConditions(pageQuery, exportVo);

                DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> dataInfo =
                    CommonFormManager.getInstance().getFormService(functionEnum).queryPageList(pageQuery);

                List<TreeMap<String, Object>> rows = dataInfo.getRows();
                if (rows.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                // 转换数据为EasyExcel需要的格式
                List<List<Object>> dataList = convertDataToExcelFormat(rows, headerFieldList, headersList);

                // 写入数据到Excel
                excelWriter.write(dataList, writeSheet);

                pageNum++;

                // 如果返回的记录数小于pageSize，说明没有更多数据了
                if (rows.size() < pageSize) {
                    hasMoreData = false;
                }
            }

            // 完成写入
            excelWriter.finish();
            outputStream.flush();

        } finally {
            // 确保资源被正确关闭
            if (excelWriter != null) {
                try {
                    excelWriter.finish();
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
        }
    }

    /**
     * 下载导入模板：依据列配置生成表头，仅含可导入的单表列
     */
    @SaIgnore()
    @GetMapping("/importTemplate/{function}")
    public void importTemplate(HttpServletResponse response, @PathVariable("function") String function) throws IOException {
        FunctionEnum functionEnum = FunctionEnum.valueOf(function);
        List<SysFieldConfig> headersList = getHeaderInfo(functionEnum);

        List<List<String>> head = new ArrayList<>();
        for (SysFieldConfig config : headersList) {
            if (isImportableColumn(config)) {
                head.add(Collections.singletonList(config.getHeaderText()));
            }
        }
        if (head.isEmpty()) {
            throw new RuntimeException("该功能未配置可导入列");
        }

        setupResponseHeaders(response, functionEnum.getDesc() + "导入模板");
        EasyExcel.write(response.getOutputStream())
            .autoCloseStream(false)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .sheet()
            .head(head)
            .doWrite(new ArrayList<>());
    }

    /**
     * 通用导入：按列配置解析Excel（首行为表头），逐行走引擎保存（含唯一性等校验），部分失败时汇总行号报错
     */
    @SaIgnore()
    @PostMapping(value = "/importData/{function}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@PathVariable("function") String function, @RequestPart("file") MultipartFile file) throws IOException {
        FunctionEnum functionEnum = FunctionEnum.valueOf(function);
        List<SysFieldConfig> headersList = getHeaderInfo(functionEnum);

        //可导入列：表头文本 -> 列名
        Map<String, String> importableColumns = new HashMap<>();
        Map<String, SysFieldConfig> columnConfigMap = new HashMap<>();
        for (SysFieldConfig config : headersList) {
            if (isImportableColumn(config)) {
                importableColumns.put(config.getHeaderText(), config.getColumnName());
                columnConfigMap.put(config.getColumnName(), config);
            }
        }
        if (importableColumns.isEmpty()) {
            return R.fail("该功能未配置可导入列");
        }

        //无模型读取：首行为表头，其余为数据行
        List<Map<Integer, String>> allRows = EasyExcel.read(file.getInputStream())
            .headRowNumber(0)
            .autoCloseStream(false)
            .sheet()
            .doReadSync();
        if (allRows.isEmpty()) {
            return R.fail("导入文件为空");
        }

        //按表头文本匹配列索引
        Map<Integer, String> columnMapping = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : allRows.get(0).entrySet()) {
            String columnName = entry.getValue() == null ? null : importableColumns.get(entry.getValue().trim());
            if (columnName != null) {
                columnMapping.put(entry.getKey(), columnName);
            }
        }
        if (columnMapping.isEmpty()) {
            return R.fail("导入文件表头与模板不一致");
        }

        synchronized (Lock.OTHER_LOCK) {
            int success = 0;
            List<String> errors = new ArrayList<>();
            for (int i = 1; i < allRows.size(); i++) {
                int rowNo = i + 1;
                Map<Integer, String> row = allRows.get(i);
                Map<String, Object> data = new HashMap<>();
                boolean emptyRow = true;
                for (Map.Entry<Integer, String> entry : columnMapping.entrySet()) {
                    String cellValue = row.get(entry.getKey());
                    if (cellValue == null || cellValue.trim().isEmpty()) {
                        continue;
                    }
                    data.put(entry.getValue(), convertImportValue(columnConfigMap.get(entry.getValue()), cellValue.trim()));
                    emptyRow = false;
                }
                if (emptyRow) {
                    continue;
                }
                Tuple2<Integer, Object> result = CommonFormManager.getInstance()
                    .getFormService(functionEnum).save(BasicBO.valueOf(data));
                if (result.getFrist() != null && result.getFrist() == 0) {
                    errors.add("第" + rowNo + "行：" + result.getSecond());
                } else {
                    success++;
                }
            }
            if (!errors.isEmpty()) {
                int limit = Math.min(errors.size(), 10);
                String detail = String.join("；", errors.subList(0, limit)) + (errors.size() > limit ? " 等" : "");
                return R.fail("成功 " + success + " 条，失败 " + errors.size() + " 条：" + detail);
            }
            return R.ok("导入成功 " + success + " 条");
        }
    }

    /**
     * 可导入列：排除关联展示列（带表前缀）、主键与审计字段
     */
    private boolean isImportableColumn(SysFieldConfig config) {
        String columnName = config.getColumnName();
        if (columnName == null || columnName.contains(".")) {
            return false;
        }
        switch (columnName) {
            case "id":
            case "creator":
            case "updater":
            case "create_time":
            case "update_time":
                return false;
            default:
                return true;
        }
    }

    /**
     * 数值列归一化：去掉无意义的小数位，避免"1.0"转整数失败
     */
    private String convertImportValue(SysFieldConfig config, String value) {
        String fieldType = config == null ? null : config.getFieldType();
        if (FieldTypeEnum.NUMBER.name().equals(fieldType) || FieldTypeEnum.NUMBER_RANGE.name().equals(fieldType)) {
            try {
                return new BigDecimal(value).stripTrailingZeros().toPlainString();
            } catch (Exception ignore) {
                return value;
            }
        }
        return value;
    }

}
