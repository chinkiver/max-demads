package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开发分支实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dev_branch")
public class DevBranch extends BaseSoftDeleteEntity {

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
     * 验证分支ID
     */
    private Long verifyBranchId;
}
