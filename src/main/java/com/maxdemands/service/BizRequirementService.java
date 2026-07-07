package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.vo.BizRequirementOverviewVO;
import com.maxdemands.vo.BizRequirementVO;

import java.util.List;

public interface BizRequirementService extends IService<BizRequirement> {
    IPage<BizRequirementVO> pageWithBatch(IPage<BizRequirement> pageParam, String status, Long batchId, String owner);

    List<BizRequirementOverviewVO> buildOverview();

    List<BizRequirementOverviewVO> buildOverviewCompleted();

    /**
     * 按状态统计业务需求数量
     */
    List<java.util.Map<String, Object>> countByStatus();
}
