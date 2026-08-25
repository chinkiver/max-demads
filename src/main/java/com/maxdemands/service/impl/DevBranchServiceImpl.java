package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public IPage<DevBranchVO> pageWithProdRequirements(IPage<DevBranch> pageParam, String reqRef) {
        // 1) 解析 reqRef（"biz:{id}" 或 "prod:{id}"）
        Long bizReqId = null;
        Long prodReqId = null;
        if (reqRef != null && !reqRef.isBlank()) {
            try {
                if (reqRef.startsWith("biz:")) {
                    bizReqId = Long.parseLong(reqRef.substring(4));
                } else if (reqRef.startsWith("prod:")) {
                    prodReqId = Long.parseLong(reqRef.substring(5));
                }
            } catch (NumberFormatException ignored) {
                // 格式错误的 reqRef 当作无筛选条件
            }
        }

        // 2) 若有筛选条件，先在 prod_requirement 里找出匹配的 dev_branch_id 集合
        List<Long> matchedDevBranchIds = null;
        if (bizReqId != null || prodReqId != null) {
            var q = Wrappers.<ProdRequirement>lambdaQuery()
                    .isNotNull(ProdRequirement::getDevBranchId);
            if (bizReqId != null) {
                q.eq(ProdRequirement::getBizReqId, bizReqId);
            }
            if (prodReqId != null) {
                q.eq(ProdRequirement::getId, prodReqId);
            }
            matchedDevBranchIds = prodRequirementMapper.selectList(q.select(ProdRequirement::getDevBranchId))
                    .stream()
                    .map(ProdRequirement::getDevBranchId)
                    .distinct()
                    .toList();
        }

        // 3) 筛选无匹配直接返回空页
        if (matchedDevBranchIds != null && matchedDevBranchIds.isEmpty()) {
            IPage<DevBranchVO> empty = new Page<>(pageParam.getCurrent(), pageParam.getSize(), 0);
            return empty;
        }

        // 4) 主查询 dev_branch
        var branchQuery = Wrappers.<DevBranch>lambdaQuery();
        if (matchedDevBranchIds != null) {
            branchQuery.in(DevBranch::getId, matchedDevBranchIds);
        }
        IPage<DevBranch> page = page(pageParam, branchQuery);

        // 5) 收集本页所有分支的 prod 关联
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
