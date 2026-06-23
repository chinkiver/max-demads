package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品需求实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_requirement")
public class ProdRequirement extends BaseSoftDeleteEntity {

    /**
     * 产品需求编号
     */
    private String prodReqCode;

    /**
     * 产品需求名称
     */
    private String prodReqName;

    /**
     * 需求摘要
     */
    private String summary;

    /**
     * 所属系统ID
     */
    private Long systemId;

    /**
     * 开发人员
     */
    private String developer;

    /**
     * 状态
     */
    private String status;

    /**
     * 关联业务需求ID
     */
    private Long bizReqId;

    /**
     * 开发分支ID
     */
    private Long devBranchId;

    /**
     * 分支操作类型
     */
    private String branchAction;
}
