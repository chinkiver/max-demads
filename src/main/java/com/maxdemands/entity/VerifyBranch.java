package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 验证分支实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("verify_branch")
public class VerifyBranch extends BaseSoftDeleteEntity {

    /**
     * 分支名称
     */
    private String branchName;

    /**
     * 所属系统ID
     */
    private Long systemId;

    /**
     * 状态
     */
    private String status;

    /**
     * 所属批次ID
     */
    private Long batchId;
}
