package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.*;
import com.maxdemands.mapper.*;
import com.maxdemands.service.BizRequirementService;
import com.maxdemands.service.DictService;
import com.maxdemands.vo.BizRequirementOverviewVO;
import com.maxdemands.vo.BizRequirementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizRequirementServiceImpl extends ServiceImpl<BizRequirementMapper, BizRequirement> implements BizRequirementService {

    private final BatchMapper batchMapper;
    private final ProdRequirementMapper prodRequirementMapper;
    private final DevBranchMapper devBranchMapper;
    private final VerifyBranchMapper verifyBranchMapper;
    private final AppSystemMapper appSystemMapper;
    private final DictService dictService;

    @Override
    public IPage<BizRequirementVO> pageWithBatch(IPage<BizRequirement> pageParam, String status, Long batchId, String owner) {
        var query = Wrappers.<BizRequirement>lambdaQuery()
                .ne(BizRequirement::getStatus, "completed")
                .eq(status != null && !status.isEmpty(), BizRequirement::getStatus, status)
                .eq(batchId != null, BizRequirement::getBatchId, batchId)
                .eq(owner != null && !owner.isEmpty(), BizRequirement::getOwner, owner)
                .orderByDesc(BizRequirement::getCreateTime);

        IPage<BizRequirement> page = page(pageParam, query);

        List<Long> batchIds = page.getRecords().stream()
                .map(BizRequirement::getBatchId)
                .distinct()
                .toList();

        List<Batch> batchList = batchIds.isEmpty()
                ? List.of()
                : batchMapper.selectList(
                        Wrappers.<Batch>lambdaQuery()
                                .in(Batch::getId, batchIds));

        List<BizRequirementVO> records = page.getRecords().stream().map(req -> {
            BizRequirementVO vo = new BizRequirementVO();
            org.springframework.beans.BeanUtils.copyProperties(req, vo);
            Batch batch = batchList.stream()
                    .filter(b -> b.getId().equals(req.getBatchId()))
                    .findFirst()
                    .orElse(null);
            if (batch != null) {
                vo.setBatchDate(batch.getBatchDate());
                vo.setBatchNo(batch.getBatchNo());
            }
            return vo;
        }).sorted((a, b) -> {
            int batchCompare;
            if (a.getBatchDate() == null && b.getBatchDate() == null) {
                batchCompare = 0;
            } else if (a.getBatchDate() == null) {
                batchCompare = 1;
            } else if (b.getBatchDate() == null) {
                batchCompare = -1;
            } else {
                batchCompare = a.getBatchDate().compareTo(b.getBatchDate());
            }
            if (batchCompare != 0) {
                return batchCompare;
            }
            return b.getCreateTime().compareTo(a.getCreateTime());
        }).collect(Collectors.toList());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizRequirementVO> resultPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public List<BizRequirementOverviewVO> buildOverview() {
        List<BizRequirement> bizList = list(Wrappers.<BizRequirement>lambdaQuery()
                .ne(BizRequirement::getStatus, "completed")
                .orderByAsc(BizRequirement::getBatchId)
                .orderByDesc(BizRequirement::getCreateTime));
        return buildOverviewFrom(bizList);
    }

    @Override
    public List<BizRequirementOverviewVO> buildOverviewCompleted() {
        List<BizRequirement> bizList = list(Wrappers.<BizRequirement>lambdaQuery()
                .eq(BizRequirement::getStatus, "completed")
                .orderByAsc(BizRequirement::getBatchId)
                .orderByDesc(BizRequirement::getCreateTime));
        return buildOverviewFrom(bizList);
    }

    private List<BizRequirementOverviewVO> buildOverviewFrom(List<BizRequirement> bizList) {
        if (bizList.isEmpty()) {
            return List.of();
        }

        List<Long> batchIds = bizList.stream()
                .map(BizRequirement::getBatchId)
                .distinct()
                .toList();
        List<Batch> batchList = batchIds.isEmpty()
                ? List.of()
                : batchMapper.selectList(
                        Wrappers.<Batch>lambdaQuery()
                                .in(Batch::getId, batchIds));
        Map<Long, Batch> batchMap = batchList.stream()
                .collect(Collectors.toMap(Batch::getId, b -> b));

        List<Long> bizIds = bizList.stream().map(BizRequirement::getId).toList();
        List<ProdRequirement> prodList = prodRequirementMapper.selectList(
                Wrappers.<ProdRequirement>lambdaQuery()
                        .in(ProdRequirement::getBizReqId, bizIds)
                        .orderByDesc(ProdRequirement::getCreateTime));

        List<Long> devBranchIds = prodList.stream()
                .map(ProdRequirement::getDevBranchId)
                .distinct()
                .toList();
        List<DevBranch> devBranchList = devBranchIds.isEmpty()
                ? List.of()
                : devBranchMapper.selectList(
                        Wrappers.<DevBranch>lambdaQuery()
                                .in(DevBranch::getId, devBranchIds));

        List<Long> verifyBranchIds = devBranchList.stream()
                .map(DevBranch::getVerifyBranchId)
                .distinct()
                .toList();
        List<VerifyBranch> verifyBranchList = verifyBranchIds.isEmpty()
                ? List.of()
                : verifyBranchMapper.selectList(
                        Wrappers.<VerifyBranch>lambdaQuery()
                                .in(VerifyBranch::getId, verifyBranchIds));

        List<Long> systemIds = prodList.stream()
                .map(ProdRequirement::getSystemId)
                .distinct()
                .toList();
        List<AppSystem> systemList = systemIds.isEmpty()
                ? List.of()
                : appSystemMapper.selectList(
                        Wrappers.<AppSystem>lambdaQuery()
                                .in(AppSystem::getId, systemIds));

        Map<Long, DevBranch> devBranchMap = devBranchList.stream()
                .collect(Collectors.toMap(DevBranch::getId, b -> b));
        Map<Long, VerifyBranch> verifyBranchMap = verifyBranchList.stream()
                .collect(Collectors.toMap(VerifyBranch::getId, b -> b));
        Map<Long, AppSystem> systemMap = systemList.stream()
                .collect(Collectors.toMap(AppSystem::getId, s -> s));

        return bizList.stream().map(biz -> {
            BizRequirementOverviewVO vo = new BizRequirementOverviewVO();
            vo.setId(biz.getId());
            vo.setReqCode(biz.getReqCode());
            vo.setReqName(biz.getReqName());
            vo.setOwner(biz.getOwner());
            vo.setStatus(biz.getStatus());
            vo.setStatusName(dictService.getDictName("biz_req_status", biz.getStatus()));
            Batch batch = batchMap.get(biz.getBatchId());
            if (batch != null) {
                vo.setBatchNo(batch.getBatchNo());
                vo.setBatchDate(batch.getBatchDate());
            }
            vo.setProdRequirements(prodList.stream()
                    .filter(p -> biz.getId().equals(p.getBizReqId()))
                    .map(p -> {
                        BizRequirementOverviewVO.ProdRequirementNode prod = new BizRequirementOverviewVO.ProdRequirementNode();
                        prod.setId(p.getId());
                        prod.setProdReqCode(p.getProdReqCode());
                        prod.setProdReqName(p.getProdReqName());
                        prod.setDeveloper(p.getDeveloper());
                        prod.setStatus(p.getStatus());
                        prod.setStatusName(dictService.getDictName("prod_req_status", p.getStatus()));
                        prod.setSystemId(p.getSystemId());
                        AppSystem system = systemMap.get(p.getSystemId());
                        prod.setSystemName(system != null ? system.getSystemName() : null);

                        DevBranch devBranch = devBranchMap.get(p.getDevBranchId());
                        if (devBranch != null) {
                            BizRequirementOverviewVO.DevBranchNode devNode = new BizRequirementOverviewVO.DevBranchNode();
                            devNode.setId(devBranch.getId());
                            devNode.setBranchName(devBranch.getBranchName());
                            devNode.setStatus(devBranch.getStatus());
                            devNode.setStatusName(dictService.getDictName("branch_status", devBranch.getStatus()));

                            VerifyBranch verifyBranch = verifyBranchMap.get(devBranch.getVerifyBranchId());
                            if (verifyBranch != null) {
                                BizRequirementOverviewVO.VerifyBranchNode verifyNode = new BizRequirementOverviewVO.VerifyBranchNode();
                                verifyNode.setId(verifyBranch.getId());
                                verifyNode.setBranchName(verifyBranch.getBranchName());
                                verifyNode.setStatus(verifyBranch.getStatus());
                                verifyNode.setStatusName(dictService.getDictName("branch_status", verifyBranch.getStatus()));
                                devNode.setVerifyBranch(verifyNode);
                            }
                            prod.setDevBranch(devNode);
                        }
                        return prod;
                    }).toList());
            return vo;
        }).toList();
    }

    @Override
    public List<Map<String, Object>> countByStatus() {
        return baseMapper.selectList(Wrappers.<BizRequirement>lambdaQuery().select(BizRequirement::getStatus))
                .stream()
                .collect(Collectors.groupingBy(BizRequirement::getStatus, Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("status", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .toList();
    }
}
