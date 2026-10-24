package com.wms.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 客户编码比较工具类
 * 用于比较Excel文件中Sheet1的客户编码与Sheet2的cust_code差异
 */
public class CustomerCodeComparison {

    /**
     * 主方法，用于测试客户编码比较功能
     */
    public static void main(String[] args) {
        // Excel文件路径（使用相对路径避免特殊字符问题）
        String excelPath = "script\\excel\\客户经营业绩分析表 (1).xlsx";
        try {
            compareCustomerCodes(excelPath);
        } catch (Exception e) {
            System.out.println("比较客户编码时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 比较Excel文件中Sheet1的客户编码与Sheet2的cust_code差异
     * @param excelPath Excel文件路径
     */
    public static void compareCustomerCodes(String excelPath) throws Exception {
        File excelFile = new File(excelPath);
        if (!excelFile.exists()) {
            throw new IllegalArgumentException("Excel文件不存在: " + excelPath);
        }

        // 读取Sheet1中的客户编码
        Set<String> sheet1Codes = readCustomerCodesFromSheet(excelPath, 0, "客户编码");
        System.out.println("Sheet1中客户编码数量: " + sheet1Codes.size());

        // 读取Sheet2中的cust_code
        Set<String> sheet2Codes = readCustomerCodesFromSheet(excelPath, 1, "cust_code");
        System.out.println("Sheet2中cust_code数量: " + sheet2Codes.size());

        // 找出Sheet1比Sheet2多的编码
        Set<String> sheet1MoreThanSheet2 = new HashSet<>(sheet1Codes);
        sheet1MoreThanSheet2.removeAll(sheet2Codes);
        System.out.println("Sheet1比Sheet2多的客户编码数量: " + sheet1MoreThanSheet2.size());
        System.out.println("Sheet1比Sheet2多的客户编码列表:");
        for (String code : new TreeSet<>(sheet1MoreThanSheet2)) {
            System.out.println(code);
        }

        // 找出Sheet1比Sheet2少的编码（即Sheet2比Sheet1多的编码）
        Set<String> sheet2MoreThanSheet1 = new HashSet<>(sheet2Codes);
        sheet2MoreThanSheet1.removeAll(sheet1Codes);
        System.out.println("Sheet1比Sheet2少的客户编码数量: " + sheet2MoreThanSheet1.size());
        System.out.println("Sheet1比Sheet2少的客户编码列表:");
        for (String code : new TreeSet<>(sheet2MoreThanSheet1)) {
            System.out.println(code);
        }
    }

    /**
     * 从Excel文件的指定工作表中读取指定列名的客户编码
     * @param excelPath Excel文件路径
     * @param sheetIndex 工作表索引（0表示第一个工作表）
     * @param columnName 列名
     * @return 客户编码集合
     */
    private static Set<String> readCustomerCodesFromSheet(String excelPath, int sheetIndex, String columnName) throws Exception {
        Set<String> codes = new HashSet<>();
        
        // 创建自定义监听器
        CodeAnalysisListener listener = new CodeAnalysisListener(columnName, codes);
        
        try (InputStream inputStream = new FileInputStream(excelPath)) {
            // 使用EasyExcel读取指定工作表
            EasyExcel.read(inputStream, listener)
                .sheet(sheetIndex)
                .doRead();
        }
        
        return codes;
    }

    /**
     * 自定义监听器，用于读取Excel中的编码数据
     */
    private static class CodeAnalysisListener extends AnalysisEventListener<Map<Integer, String>> {
        private String targetColumnName;
        private Set<String> codes;
        private Map<Integer, String> headerMap = new HashMap<>();
        private Integer targetColumnIndex = null;
        private boolean isHeaderRead = false;

        public CodeAnalysisListener(String targetColumnName, Set<String> codes) {
            this.targetColumnName = targetColumnName;
            this.codes = codes;
        }

        @Override
        public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
            // 第一行是表头
            if (!isHeaderRead) {
                // 保存表头信息
                headerMap.putAll(rowData);
                // 查找目标列的索引
                for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().equals(targetColumnName)) {
                        targetColumnIndex = entry.getKey();
                        break;
                    }
                }
                isHeaderRead = true;
                return;
            }

            // 如果找到了目标列
            if (targetColumnIndex != null) {
                String code = rowData.get(targetColumnIndex);
                if (code != null && !code.trim().isEmpty()) {
                    codes.add(code.trim());
                }
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 所有数据解析完成后执行的操作
        }
    }
}