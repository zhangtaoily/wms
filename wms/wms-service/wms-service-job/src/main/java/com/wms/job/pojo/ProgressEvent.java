package com.wms.job.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import static com.wms.common.utils.CalcUtils.calculateProgress;

/**
 * @author li shuang
 */
@Data
@AllArgsConstructor
public class ProgressEvent {
    private int progress;
    private String message;
    // enum: RUNNING, COMPLETED, FAILED, CANCELLED
    private int status;
    private int processedItems;
    private int totalItems;
    private String uid;

    public ProgressEvent(String message, int status, int processedItems, int totalItems, String uid) {
        this.message = message;
        this.status = status;
        this.progress = calculateProgress(processedItems, totalItems);
        this.processedItems = processedItems;
        this.totalItems = totalItems;
        this.uid = uid;
    }
}
