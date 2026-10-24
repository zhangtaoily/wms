package com.wms.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wac
 * @date 2024/11/5
 * @Description
 */
@Data
public class PushDownVo {

    private Long id;

    private List<Long> detailsId;

}
