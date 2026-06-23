package com.maxdemands.dto;

import lombok.Data;

@Data
public class ProdRequirementDTO {
    private Long id;
    private String prodReqCode;
    private String prodReqName;
    private String summary;
    private Long systemId;
    private String systemName;
    private String developer;
    private String status;
    private Long bizReqId;
    private String branchAction;
    private Long devBranchId;

    /** 关联开发分支名称 */
    private String devBranchName;
    /** 关联开发分支状态 */
    private String devBranchStatus;
    /** 关联验证分支名称 */
    private String verifyBranchName;
    /** 关联验证分支状态 */
    private String verifyBranchStatus;
}
