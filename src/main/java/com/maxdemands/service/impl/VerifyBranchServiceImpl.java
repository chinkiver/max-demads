package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.entity.ProdRequirement;
import com.maxdemands.entity.VerifyBranch;
import com.maxdemands.mapper.BizRequirementMapper;
import com.maxdemands.mapper.DevBranchMapper;
import com.maxdemands.mapper.ProdRequirementMapper;
import com.maxdemands.mapper.VerifyBranchMapper;
import com.maxdemands.service.VerifyBranchService;
import com.maxdemands.vo.VerifyBranchRelationTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerifyBranchServiceImpl extends ServiceImpl<VerifyBranchMapper, VerifyBranch> implements VerifyBranchService {

    private final DevBranchMapper devBranchMapper;
    private final ProdRequirementMapper prodRequirementMapper;
    private final BizRequirementMapper bizRequirementMapper;

    @Override
    public VerifyBranchRelationTreeVO buildRelationTree(Long verifyBranchId) {
        VerifyBranch verifyBranch = getById(verifyBranchId);
        if (verifyBranch == null) {
            return null;
        }

        VerifyBranchRelationTreeVO root = new VerifyBranchRelationTreeVO();
        root.setId(verifyBranch.getId());
        root.setLabel(verifyBranch.getBranchName());
        root.setType("verifyBranch");

        List<DevBranch> devBranches = devBranchMapper.selectList(
                Wrappers.<DevBranch>lambdaQuery()
                        .eq(DevBranch::getVerifyBranchId, verifyBranchId)
                        .orderByDesc(DevBranch::getCreateTime));

        if (devBranches.isEmpty()) {
            return root;
        }

        List<Long> devBranchIds = devBranches.stream().map(DevBranch::getId).toList();
        List<ProdRequirement> prodList = prodRequirementMapper.selectList(
                Wrappers.<ProdRequirement>lambdaQuery()
                        .in(ProdRequirement::getDevBranchId, devBranchIds)
                        .orderByDesc(ProdRequirement::getCreateTime));

        List<Long> bizReqIds = prodList.stream()
                .map(ProdRequirement::getBizReqId)
                .distinct()
                .toList();
        List<BizRequirement> bizList = bizReqIds.isEmpty()
                ? List.of()
                : bizRequirementMapper.selectList(
                        Wrappers.<BizRequirement>lambdaQuery()
                                .in(BizRequirement::getId, bizReqIds));

        root.setChildren(devBranches.stream().map(devBranch -> {
            VerifyBranchRelationTreeVO devNode = new VerifyBranchRelationTreeVO();
            devNode.setId(devBranch.getId());
            devNode.setLabel(devBranch.getBranchName());
            devNode.setType("devBranch");

            List<ProdRequirement> devProdList = prodList.stream()
                    .filter(p -> devBranch.getId().equals(p.getDevBranchId()))
                    .toList();

            devNode.setChildren(devProdList.stream().map(prod -> {
                VerifyBranchRelationTreeVO prodNode = new VerifyBranchRelationTreeVO();
                prodNode.setId(prod.getId());
                prodNode.setLabel(prod.getProdReqCode() + "-" + prod.getProdReqName());
                prodNode.setType("prodRequirement");

                BizRequirement biz = bizList.stream()
                        .filter(b -> b.getId().equals(prod.getBizReqId()))
                        .findFirst()
                        .orElse(null);

                if (biz != null) {
                    VerifyBranchRelationTreeVO bizNode = new VerifyBranchRelationTreeVO();
                    bizNode.setId(biz.getId());
                    bizNode.setLabel(biz.getReqCode() + "-" + biz.getReqName());
                    bizNode.setType("bizRequirement");
                    prodNode.setChildren(List.of(bizNode));
                }

                return prodNode;
            }).toList());

            return devNode;
        }).toList());

        return root;
    }
}
