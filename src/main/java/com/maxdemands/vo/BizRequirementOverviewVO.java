package com.maxdemands.vo;

import lombok.Data;

import java.util.List;

@Data
public class BizRequirementOverviewVO {

    private Long id;
    private String reqCode;
    private String reqName;
    private String owner;
    private String status;
    private String statusName;
    private List<ProdRequirementNode> prodRequirements;

    @Data
    public static class ProdRequirementNode {
        private Long id;
        private String prodReqCode;
        private String prodReqName;
        private String developer;
        private String status;
        private String statusName;
        private Long systemId;
        private String systemName;
        private DevBranchNode devBranch;
    }

    @Data
    public static class DevBranchNode {
        private Long id;
        private String branchName;
        private String status;
        private String statusName;
        private VerifyBranchNode verifyBranch;
    }

    @Data
    public static class VerifyBranchNode {
        private Long id;
        private String branchName;
        private String status;
        private String statusName;
    }
}
