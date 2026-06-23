package com.maxdemands.vo;

import lombok.Data;

import java.util.List;

@Data
public class DevBranchVO {
    private Long id;
    private String branchName;
    private Long systemId;
    private String status;
    private Long verifyBranchId;
    private List<ProdRequirementItemVO> prodRequirements;

    @Data
    public static class ProdRequirementItemVO {
        private Long id;
        private String prodReqCode;
        private String prodReqName;
        private Long bizReqId;
        private String bizReqCode;
        private String bizReqName;
    }
}
