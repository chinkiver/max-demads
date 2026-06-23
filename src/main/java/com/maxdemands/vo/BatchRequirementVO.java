package com.maxdemands.vo;

import lombok.Data;

import java.util.List;

@Data
public class BatchRequirementVO {
    private Long id;
    private String reqCode;
    private String reqName;
    private String reqCategory;
    private String priority;
    private String proposer;
    private String proposerDept;
    private String owner;
    private String status;
    private List<ProdRequirementItemVO> prodRequirements;

    @Data
    public static class ProdRequirementItemVO {
        private Long id;
        private String prodReqCode;
        private String prodReqName;
        private String developer;
        private String status;
        private String devBranchName;
        private String devBranchStatus;
        private String verifyBranchName;
        private String verifyBranchStatus;
    }
}
