package com.maxdemands.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.VerifyBranch;
import com.maxdemands.vo.VerifyBranchRelationTreeVO;

public interface VerifyBranchService extends IService<VerifyBranch> {
    VerifyBranchRelationTreeVO buildRelationTree(Long verifyBranchId);
}
