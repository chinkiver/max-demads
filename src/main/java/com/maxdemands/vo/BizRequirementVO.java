package com.maxdemands.vo;

import com.maxdemands.entity.BizRequirement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class BizRequirementVO extends BizRequirement {
    private LocalDate batchDate;
    private String batchNo;
}
