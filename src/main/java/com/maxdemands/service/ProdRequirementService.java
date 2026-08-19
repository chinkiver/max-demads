package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.dto.ProdRequirementDTO;
import com.maxdemands.entity.ProdRequirement;

public interface ProdRequirementService extends IService<ProdRequirement> {
    void createWithBranch(ProdRequirementDTO dto);
    void updateWithBranch(ProdRequirementDTO dto);
    void deleteWithBranch(Long id);

    IPage<ProdRequirementDTO> pageWithBranches(IPage<ProdRequirement> pageParam, Long bizReqId, String developer, Boolean excludeCompleted);
}
