package com.wms.common.api;

import cn.hutool.http.HttpStatus;
import com.wms.common.core.page.TableDataInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 描述一个基本的 API 请求，注意以下：
 * <p>
 * 如果子类实现中，有与父类相同名字的属性，却类型不同，此时，可正常编译，在运行期才会抛出异常。
 */
@Setter
@Getter
@SuppressWarnings("all")
public class BasicVO<T> implements VO {

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;


    /**
     * 消息内容
     */
    private T data;

    public static <T> BasicVO<T> build(T obj) {
        BasicVO<T> rspData = new BasicVO<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setData(obj);
        return rspData;
    }
}
