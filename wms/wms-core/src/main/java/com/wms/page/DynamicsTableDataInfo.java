package com.wms.page;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.domain.SysFieldConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 表格分页数据对象
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
public class DynamicsTableDataInfo<T,H> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表头数据
     */
    private List<H> headers;

    /**
     * 列表数据
     */
    private List<T> rows;

    /**
     * 数值列合计行（SumRowPlugin 产出）
     */
    private Map<String, Object> sumRow;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public DynamicsTableDataInfo(List<H> headers, List<T> rows, long total) {
        this.headers = headers;
        this.rows = rows;
        this.total = total;
    }

    public static <T,H> DynamicsTableDataInfo<T,H> build(List<H> headers,IPage<T> page) {
        DynamicsTableDataInfo<T,H> rspData = new DynamicsTableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setHeaders(headers);
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    public static <T,H> DynamicsTableDataInfo<T,H> build(List<T> list) {
        DynamicsTableDataInfo<T,H> rspData = new DynamicsTableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(list.size());
        return rspData;
    }

    public static <T,H> DynamicsTableDataInfo<T,H> build() {
        DynamicsTableDataInfo<T,H> rspData = new DynamicsTableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        return rspData;
    }

    /**
     * 是否超过导出限制
     * @return
     */
    public boolean isMoreThanExportLimit(){
        return total>50000;
    }
}
