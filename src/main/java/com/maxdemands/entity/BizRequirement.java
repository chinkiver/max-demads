package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务需求实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_requirement")
public class BizRequirement extends BaseSoftDeleteEntity {

    /**
     * 需求编号
     */
    private String reqCode;

    /**
     * 需求名称
     */
    private String reqName;

    /**
     * 需求分类
     */
    private String reqCategory;

    /**
     * 需求摘要
     */
    private String summary;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 提出人
     */
    private String proposer;

    /**
     * 提出部门
     */
    private String proposerDept;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 状态
     */
    private String status;

    /**
     * 所属批次ID
     */
    private Long batchId;

    /**
     * 实际投产日期
     */
    private java.time.LocalDate productionDate;
}
