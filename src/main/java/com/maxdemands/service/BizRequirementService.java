package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.vo.BizRequirementOverviewVO;
import com.maxdemands.vo.BizRequirementVO;

import java.util.List;

public interface BizRequirementService extends IService<BizRequirement> {
    /**
     * 分页查询业务需求
     *
     * @param pageParam 分页参数
     * @param status    状态（精确）
     * @param batchId   批次ID（精确）
     * @param owner     负责人（精确）
     * @param reqCode   需求编码（包含匹配，传 null/空不筛）
     * @param reqName   需求名称（包含匹配，传 null/空不筛）
     */
    IPage<BizRequirementVO> pageWithBatch(IPage<BizRequirement> pageParam, String status, Long batchId, String owner, String reqCode, String reqName);

    List<BizRequirementOverviewVO> buildOverview();

    List<BizRequirementOverviewVO> buildOverviewCompleted();

    /**
     * 按状态统计业务需求数量
     */
    List<java.util.Map<String, Object>> countByStatus();
}
