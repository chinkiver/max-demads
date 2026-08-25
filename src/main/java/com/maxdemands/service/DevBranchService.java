package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.vo.DevBranchVO;

public interface DevBranchService extends IService<DevBranch> {
    /**
     * 分页查询开发分支，附带关联的产品需求（含业务需求信息）
     *
     * @param pageParam 分页参数
     * @param reqRef    关联需求筛选，格式 "biz:{id}"（按业务需求筛）或 "prod:{id}"（按产品需求筛），传 null/空不过滤
     */
    IPage<DevBranchVO> pageWithProdRequirements(IPage<DevBranch> pageParam, String reqRef);
}
