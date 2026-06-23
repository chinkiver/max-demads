package com.maxdemands.vo;

import lombok.Data;

import java.util.List;

@Data
public class VerifyBranchRelationTreeVO {
    private Long id;
    private String label;
    private String type;
    private List<VerifyBranchRelationTreeVO> children;
}
