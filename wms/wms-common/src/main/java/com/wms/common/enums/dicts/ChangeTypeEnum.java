package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 变更类型枚举
 * @author li shuang
 */
@Getter
@AllArgsConstructor
public enum ChangeTypeEnum {
    t000("000", "基础变更"),
    t001("001", "客户更改"),
    t002("002", "纱支更改"),
    t003("003", "染色类别更改（改单价）"),
    t004("004", "合同内不同原料调整数量"),
    t005("005", "合同外相同原料，更改化学处理方式"),
    t006("006", "合同外变更产品品种"),
    t007("007", "合同总数量更改"),
    t008("008", "合同类型更改"),
    t009("009", "价格更改"),
    t010("010", "合同取消"),
    t011("011", "延长合同期限"),
    t012("012", "合同外更改原料"),
    t013("013", "合同数量更改"),
    t014("014", "基准价更改"),
    t015("015", "计重方式变更"),
    t016("016", "销售组织变更"),
    t017("017", "集锦卡变更"),
    t018("018", "特殊工艺变更"),
    t019("019", "佣金变更"),
    t020("020", "扩展"),
    t021("021", "扩展"),
    t022("022", "扩展"),
    t023("023", "扩展"),
    t024("024", "扩展"),
    t025("025", "扩展"),
    t026("026", "扩展"),
    t027("027", "扩展"),
    t028("028", "染色类别更改（不改单价）"),
    t029("027", "扩展"),
    t030("030", "扩展"),
    t031("031", "扩展"),
    t032("032", "扩展"),
    t033("033", "扩展"),
    t034("034", "扩展"),
    t035("035", "扩展"),
    t036("036", "扩展"),
    t037("037", "扩展"),
    t038("038", "扩展"),
    t039("039", "扩展"),

    // 订单变更类型
    t1001("1001", "数量变更"),
    t100101("100101", "数量变更-增加数量"),
    t100102("100102", "数量变更-减少数量"),
    t1002("1002", "客户色号（绞筒染不能改）"),
    t1003("1003", "指定胚纱批号"),
    t1004("1004", "确认意见"),
    t1005("1005", "色牢度"),
    t1006("1006", "成品包装要求"),
    t1007("1007", "绞装/筒装"),
    t1008("1008", "计重方式"),
    t1009("1009", "是否小样"),
    t1010("1010", "是否上蜡"),
    t1011("1011", "小样数量"),
    t1012("1012", "中样数量（绞筒染不能改）"),
    t1013("1013", "特殊工艺"),
    t1014("1014", "产品用途"),
    t1015("1015", "品牌"),
    t1016("1016", "小样信息（绞筒染不能改）"),
    t1017("1017", "请即处理"),
    t1018("1018", "其他注意事项"),
    t1019("1019", "标签打印要求"),
    t1020("1020", "染色要求（条染、胚纱不更新）"),
    t1021("1021", "工艺及纺纱要求（绞筒染不能改）"),
    t1022("1022", "删除集锦卡"),
    t1023("1023", "订单引合同数据"),
    t1024("1024", "是否打样"),
    t1025("1025", "公司备纱加急"),
    t1026("1026", "再次加急"),
    t1027("1027", "是否快返"),
    t1028("1028", "成品要求"),
    t1029("1029", "打印标签"),
    t1030("1030", "取消加急"),
    t1031("1031", "取消加急（停用）"),
    t1032("1032", "指定胚纱批号变更"),
    t1033("1033", "交清期变更"),
    t1034("1034", "扩展"),
    t1035("1035", "扩展"),
    t1036("1036", "扩展"),
    t1037("1037", "扩展"),
    t1038("1038", "扩展"),
    t1039("1039", "扩展"),
    t1040("1040", "扩展"),
    t1041("1041", "扩展"),
    t1042("1042", "扩展"),
    t1043("1043", "扩展"),
    t1044("1044", "扩展"),
    t1045("1045", "扩展"),
    t1046("1046", "扩展"),
    t1047("1047", "扩展"),
    t1048("1048", "扩展"),
    t1049("1049", "扩展");

    private final String code;
    private final String name;

    /**
     * 根据code查找对应的ChangeTypeEnum
     *
     * @param code 变更类型的代码
     * @return 对应的ChangeTypeEnum，如果未找到则返回null
     */
    public static ChangeTypeEnum fromCode(String code) {
        for (ChangeTypeEnum changeType : ChangeTypeEnum.values()) {
            if (changeType.getCode().equals(code)) {
                return changeType;
            }
        }
        throw new IllegalArgumentException("Unknown function code: " + code);
    }

    /**
     * 根据code查找对应的ChangeTypeEnum
     *
     * @param code 变更类型的代码
     * @return 对应的ChangeTypeEnum，如果未找到则返回null
     */
    public static ArrayList<ChangeTypeEnum> fromCodeList(String code) {
        ArrayList<ChangeTypeEnum> result = new ArrayList<>();
        // 将字符串按逗号分隔成数组
        String[] codeArray = code.split(",");

        for (String codeItem : codeArray) {
            // 去除空格并调用 fromCode 方法
            ChangeTypeEnum changeType = fromCode(codeItem.trim());
            result.add(changeType);
        }
        return result;
    }
    /**
     * 将ChangeTypeEnum列表转换为以逗号分隔的代码字符串
     *
     * @param changeTypeEnums ChangeTypeEnum的列表
     * @return 以逗号分隔的代码字符串
     */
    public static String toCodeString(List<ChangeTypeEnum> changeTypeEnums) {
        return changeTypeEnums.stream()
            // 获取每个枚举的代码
            .map(ChangeTypeEnum::getCode)
            // 使用逗号连接成一个字符串
            .collect(Collectors.joining(","));
    }
}
