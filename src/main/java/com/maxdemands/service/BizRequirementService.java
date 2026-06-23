package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.vo.BizRequirementVO;

public interface BizRequirementService extends IService<BizRequirement> {
    IPage<BizRequirementVO> pageWithBatch(IPage<BizRequirement> pageParam, String status, Long batchId);
}
