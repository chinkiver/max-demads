package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.dto.ProdRequirementDTO;
import com.maxdemands.entity.ProdRequirement;

public interface ProdRequirementService extends IService<ProdRequirement> {
    void createWithBranch(ProdRequirementDTO dto);
    void updateWithBranch(ProdRequirementDTO dto);
    void deleteWithBranch(Long id);

    /**
     * 分页查询产品需求
     *
     * @param pageParam       分页参数
     * @param bizReqId        关联业务需求ID（精确）
     * @param developer       开发人员（精确）
     * @param excludeCompleted 是否排除已完成
     * @param prodReqCode     产品需求编码（包含匹配，传 null/空不筛）
     * @param prodReqName     产品需求名称（包含匹配，传 null/空不筛）
     */
    IPage<ProdRequirementDTO> pageWithBranches(IPage<ProdRequirement> pageParam, Long bizReqId, String developer, Boolean excludeCompleted, String prodReqCode, String prodReqName);
}
