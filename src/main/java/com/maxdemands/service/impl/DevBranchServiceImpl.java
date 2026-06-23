package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.entity.ProdRequirement;
import com.maxdemands.mapper.BizRequirementMapper;
import com.maxdemands.mapper.DevBranchMapper;
import com.maxdemands.mapper.ProdRequirementMapper;
import com.maxdemands.service.DevBranchService;
import com.maxdemands.vo.DevBranchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevBranchServiceImpl extends ServiceImpl<DevBranchMapper, DevBranch> implements DevBranchService {

    private final ProdRequirementMapper prodRequirementMapper;
    private final BizRequirementMapper bizRequirementMapper;

    @Override
    public IPage<DevBranchVO> pageWithProdRequirements(IPage<DevBranch> pageParam) {
        IPage<DevBranch> page = page(pageParam);

        List<Long> branchIds = page.getRecords().stream()
                .map(DevBranch::getId)
                .toList();

        List<ProdRequirement> allProdList = branchIds.isEmpty()
                ? List.of()
                : prodRequirementMapper.selectList(
                        Wrappers.<ProdRequirement>lambdaQuery()
                                .in(ProdRequirement::getDevBranchId, branchIds)
                                .orderByDesc(ProdRequirement::getCreateTime));

        List<Long> bizReqIds = allProdList.stream()
                .map(ProdRequirement::getBizReqId)
                .distinct()
                .toList();
        List<BizRequirement> bizReqList = bizReqIds.isEmpty()
                ? List.of()
                : bizRequirementMapper.selectList(
                        Wrappers.<BizRequirement>lambdaQuery()
                                .in(BizRequirement::getId, bizReqIds));

        return page.convert(branch -> {
            DevBranchVO vo = new DevBranchVO();
            vo.setId(branch.getId());
            vo.setBranchName(branch.getBranchName());
            vo.setSystemId(branch.getSystemId());
            vo.setStatus(branch.getStatus());
            vo.setVerifyBranchId(branch.getVerifyBranchId());
            vo.setProdRequirements(allProdList.stream()
                    .filter(p -> branch.getId().equals(p.getDevBranchId()))
                    .map(p -> {
                        DevBranchVO.ProdRequirementItemVO item = new DevBranchVO.ProdRequirementItemVO();
                        item.setId(p.getId());
                        item.setProdReqCode(p.getProdReqCode());
                        item.setProdReqName(p.getProdReqName());
                        item.setBizReqId(p.getBizReqId());
                        BizRequirement biz = bizReqList.stream()
                                .filter(b -> b.getId().equals(p.getBizReqId()))
                                .findFirst()
                                .orElse(null);
                        if (biz != null) {
                            item.setBizReqCode(biz.getReqCode());
                            item.setBizReqName(biz.getReqName());
                        }
                        return item;
                    }).toList());
            return vo;
        });
    }
}
