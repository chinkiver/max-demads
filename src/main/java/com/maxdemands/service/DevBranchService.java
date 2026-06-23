package com.maxdemands.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.vo.DevBranchVO;

public interface DevBranchService extends IService<DevBranch> {
    IPage<DevBranchVO> pageWithProdRequirements(IPage<DevBranch> pageParam);
}
