package com.wms.common.api;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.ObjectUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述一个基本的 API 请求，注意以下：
 * <p>
 * 如果子类实现中，有与父类相同名字的属性，却类型不同，此时，可正常编译，在运行期才会抛出异常。
 */
@Setter
@Getter
@SuppressWarnings("all")
public class BasicBO implements BO, ValidateAble {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

    /**
     * 主、子、孙是树形结构
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> data = new HashMap<>();

    /**
     * 批量操作
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private List<Map<String, Object>> datas = ListUtils.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private List<String> detailId = ListUtils.empty();

    /**
     * 是否变更单保存
     */
    private boolean isChange = false;

    public void setChange(boolean change) {
        isChange = change;
    }
    public boolean getChange() {
        return isChange;
    }

    @Override
    public void validate() {

    }

    @JsonIgnore
    public boolean isAdd() {
        return !this.getData().containsKey("id") || this.getData().get("id") == null;
    }

    public static BasicBO valueOf(Map<String, Object> data) {
        BasicBO bo = new BasicBO();
        bo.setData(data);
        return bo;
    }

    public boolean isCheckCridet() {
        return this.getParams().get("isContinueShipping") == null;
    }
}
