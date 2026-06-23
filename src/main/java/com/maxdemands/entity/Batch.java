package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 批次实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("batch")
public class Batch extends BaseSoftDeleteEntity {

    /**
     * 批次编号
     */
    private String batchNo;

    /**
     * 批次类型
     */
    private String batchType;

    /**
     * 批次日期
     */
    private LocalDate batchDate;

    /**
     * 状态
     */
    private String status;
}
