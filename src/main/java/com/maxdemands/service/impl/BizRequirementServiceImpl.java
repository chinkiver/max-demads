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

import java.time.LocalDate;
import java.util.Comparator;
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
    public IPage<BizRequirementVO> pageWithBatch(IPage<BizRequirement> pageParam, String status, Long batchId, String owner, String reqCode, String reqName) {
        var query = Wrappers.<BizRequirement>lambdaQuery()
                .ne(BizRequirement::getStatus, "completed")
                .eq(status != null && !status.isEmpty(), BizRequirement::getStatus, status)
                .eq(batchId != null, BizRequirement::getBatchId, batchId)
                .eq(owner != null && !owner.isEmpty(), BizRequirement::getOwner, owner)
                .like(reqCode != null && !reqCode.isEmpty(), BizRequirement::getReqCode, reqCode)
                .like(reqName != null && !reqName.isEmpty(), BizRequirement::getReqName, reqName)
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
        Map<Long, Batch> batchMap = batchList.stream()
                .collect(Collectors.toMap(Batch::getId, b -> b));

        // 排序：与 buildOverviewFrom 共用 compareByBatchThenCreateTime，列表 / 全览 顺序永远一致
        List<BizRequirement> sorted = page.getRecords().stream()
                .sorted((a, b) -> compareByBatchThenCreateTime(a, b, batchMap))
                .toList();

        List<BizRequirementVO> records = sorted.stream().map(req -> {
            BizRequirementVO vo = new BizRequirementVO();
            org.springframework.beans.BeanUtils.copyProperties(req, vo);
            Batch batch = batchMap.get(req.getBatchId());
            if (batch != null) {
                vo.setBatchDate(batch.getBatchDate());
                vo.setBatchNo(batch.getBatchNo());
            }
            return vo;
        }).collect(Collectors.toList());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizRequirementVO> resultPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public List<BizRequirementOverviewVO> buildOverview() {
        List<BizRequirement> bizList = list(Wrappers.<BizRequirement>lambdaQuery()
                .ne(BizRequirement::getStatus, "completed"));
        return buildOverviewFrom(bizList);
    }

    @Override
    public List<BizRequirementOverviewVO> buildOverviewCompleted() {
        List<BizRequirement> bizList = list(Wrappers.<BizRequirement>lambdaQuery()
                .eq(BizRequirement::getStatus, "completed"));
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

        // 排序：与 pageWithBatch 完全一致 —— batchDate 升序（最近批次靠前、最紧急优先），
        //       同批次内 createTime 降序（最新创建的在上）。
        // SQL 不排序，由 Java 用统一 comparator 处理，避免两处逻辑漂移。
        List<BizRequirement> sortedBizList = bizList.stream()
                .sorted((a, b) -> compareByBatchThenCreateTime(a, b, batchMap))
                .toList();

        List<Long> bizIds = sortedBizList.stream().map(BizRequirement::getId).toList();
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

        return sortedBizList.stream().map(biz -> {
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
            vo.setProductionDate(biz.getProductionDate());
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

    /**
     * 业务需求统一排序规则（列表 / 需求全览 共用）：
     * 1) batchDate 升序 —— 最近批次（最紧急）优先；无批次排到最后
     * 2) createTime 降序 —— 同批次内最新创建在上
     */
    private static int compareByBatchThenCreateTime(BizRequirement a, BizRequirement b, Map<Long, Batch> batchMap) {
        LocalDate ad = batchMap.get(a.getBatchId()) != null ? batchMap.get(a.getBatchId()).getBatchDate() : null;
        LocalDate bd = batchMap.get(b.getBatchId()) != null ? batchMap.get(b.getBatchId()).getBatchDate() : null;
        int batchCompare;
        if (ad == null && bd == null) {
            batchCompare = 0;
        } else if (ad == null) {
            batchCompare = 1;
        } else if (bd == null) {
            batchCompare = -1;
        } else {
            batchCompare = ad.compareTo(bd);
        }
        if (batchCompare != 0) {
            return batchCompare;
        }
        return b.getCreateTime().compareTo(a.getCreateTime());
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
