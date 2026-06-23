package com.maxdemands.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AutoGenerateResultVO {
    private LocalDate batchDate;
    private String batchType;
    private String batchNo;
    private String result;
}
