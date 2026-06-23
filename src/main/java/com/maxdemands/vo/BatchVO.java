package com.maxdemands.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BatchVO {
    private Long id;
    private String batchNo;
    private String batchType;
    private LocalDate batchDate;
    private String status;
    private List<RequirementItem> requirements;

    @Data
    public static class RequirementItem {
        private Long id;
        private String reqCode;
        private String reqName;
    }
}
