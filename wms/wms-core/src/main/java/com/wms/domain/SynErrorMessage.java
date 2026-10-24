package com.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("syn_error_message")
public class SynErrorMessage {
    private String docCode;
    private String createBy;
    private String docType;
    private Integer docTypeNum;
    private LocalDate docDate;
    private Long docId;
    private String errorMsg;
}
