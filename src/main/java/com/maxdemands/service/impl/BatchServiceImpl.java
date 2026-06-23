package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.Batch;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.entity.ProdRequirement;
import com.maxdemands.entity.VerifyBranch;
import com.maxdemands.mapper.BatchMapper;
import com.maxdemands.mapper.BizRequirementMapper;
import com.maxdemands.mapper.DevBranchMapper;
import com.maxdemands.mapper.ProdRequirementMapper;
import com.maxdemands.mapper.VerifyBranchMapper;
import com.maxdemands.service.BatchService;
import com.maxdemands.service.DictService;
import com.maxdemands.vo.AutoGenerateResultVO;
import com.maxdemands.vo.BatchRequirementVO;
import com.maxdemands.vo.BatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {

    private final DictService dictService;
    private final BizRequirementMapper bizRequirementMapper;
    private final ProdRequirementMapper prodRequirementMapper;
    private final DevBranchMapper devBranchMapper;
    private final VerifyBranchMapper verifyBranchMapper;

    @Override
    @Transactional
    public List<AutoGenerateResultVO> autoGenerate(String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();
        List<AutoGenerateResultVO> results = new ArrayList<>();

        LocalDate firstDayOfMonth = LocalDate.of(year, monthValue, 1);
        LocalDate firstThursday = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.THURSDAY));

        List<LocalDate> thursdays = new ArrayList<>();
        LocalDate current = firstThursday;
        while (current.getMonthValue() == monthValue) {
            thursdays.add(current);
            current = current.plusWeeks(1);
        }

        String[] typeMap = {"standard_production", "routine_production", "standard_production", "routine_production"};

        for (int i = 0; i < thursdays.size() && i < 4; i++) {
            LocalDate date = thursdays.get(i);
            String type = typeMap[i];
            String typeName = dictService.getDictName("batch_type", type);
            String batchNo = date.toString().replace("-", "") + "-" + typeName;

            AutoGenerateResultVO result = new AutoGenerateResultVO();
            result.setBatchDate(date);
            result.setBatchType(typeName);
            result.setBatchNo(batchNo);

            long count = count(Wrappers.<Batch>lambdaQuery()
                    .eq(Batch::getBatchNo, batchNo));

            if (count > 0) {
                result.setResult("existed");
            } else {
                Batch batch = new Batch();
                batch.setBatchNo(batchNo);
                batch.setBatchType(type);
                batch.setBatchDate(date);
                batch.setStatus("planning");
                save(batch);
                result.setResult("success");
            }
            results.add(result);
        }

        return results;
    }

    @Override
    @Transactional
    public int autoUpdateCompleted() {
        LocalDate today = LocalDate.now();
        List<Batch> toUpdate = list(Wrappers.<Batch>lambdaQuery()
                .lt(Batch::getBatchDate, today)
                .ne(Batch::getStatus, "completed"));

        if (toUpdate.isEmpty()) {
            return 0;
        }

        for (Batch batch : toUpdate) {
            batch.setStatus("completed");
        }
        updateBatchById(toUpdate);
        return toUpdate.size();
    }

    @Override
    @Transactional
    public void updateBatch(Long id, Batch batch) {
        Batch existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("批次不存在");
        }
        String typeName = dictService.getDictName("batch_type", batch.getBatchType());
        batch.setBatchNo(batch.getBatchDate().toString().replace("-", "") + "-" + typeName);
        batch.setId(id);
        updateById(batch);
    }

    @Override
    @Transactional
    public void deleteBatch(Long id) {
        long bizCount = bizRequirementMapper.selectCount(
                Wrappers.<BizRequirement>lambdaQuery()
                        .eq(BizRequirement::getBatchId, id));
        if (bizCount > 0) {
            throw new IllegalStateException("该批次已关联业务需求，无法删除");
        }

        long verifyCount = verifyBranchMapper.selectCount(
                Wrappers.<VerifyBranch>lambdaQuery()
                        .eq(VerifyBranch::getBatchId, id));
        if (verifyCount > 0) {
            throw new IllegalStateException("该批次已关联验证分支，无法删除");
        }

        removeById(id);
    }

    @Override
    public List<BatchRequirementVO> listRequirements(Long batchId) {
        List<BizRequirement> bizList = bizRequirementMapper.selectList(
                Wrappers.<BizRequirement>lambdaQuery()
                        .eq(BizRequirement::getBatchId, batchId)
                        .orderByDesc(BizRequirement::getCreateTime));

        return bizList.stream().map(biz -> {
            BatchRequirementVO vo = new BatchRequirementVO();
            vo.setId(biz.getId());
            vo.setReqCode(biz.getReqCode());
            vo.setReqName(biz.getReqName());
            vo.setReqCategory(biz.getReqCategory());
            vo.setPriority(biz.getPriority());
            vo.setProposer(biz.getProposer());
            vo.setProposerDept(biz.getProposerDept());
            vo.setOwner(biz.getOwner());
            vo.setStatus(biz.getStatus());

            List<ProdRequirement> prodList = prodRequirementMapper.selectList(
                    Wrappers.<ProdRequirement>lambdaQuery()
                            .eq(ProdRequirement::getBizReqId, biz.getId())
                            .orderByDesc(ProdRequirement::getCreateTime));

            vo.setProdRequirements(prodList.stream().map(prod -> {
                BatchRequirementVO.ProdRequirementItemVO item = new BatchRequirementVO.ProdRequirementItemVO();
                item.setId(prod.getId());
                item.setProdReqCode(prod.getProdReqCode());
                item.setProdReqName(prod.getProdReqName());
                item.setDeveloper(prod.getDeveloper());
                item.setStatus(prod.getStatus());

                Long devBranchId = prod.getDevBranchId();
                if (devBranchId != null) {
                    DevBranch devBranch = devBranchMapper.selectById(devBranchId);
                    if (devBranch != null) {
                        item.setDevBranchName(devBranch.getBranchName());
                        item.setDevBranchStatus(devBranch.getStatus());
                        Long verifyBranchId = devBranch.getVerifyBranchId();
                        if (verifyBranchId != null) {
                            VerifyBranch verifyBranch = verifyBranchMapper.selectById(verifyBranchId);
                            if (verifyBranch != null) {
                                item.setVerifyBranchName(verifyBranch.getBranchName());
                                item.setVerifyBranchStatus(verifyBranch.getStatus());
                            }
                        }
                    }
                }
                return item;
            }).toList());

            return vo;
        }).toList();
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<BatchVO> pageWithRequirements(
            com.baomidou.mybatisplus.core.metadata.IPage<Batch> pageParam) {
        return pageWithRequirements(pageParam, Wrappers.<Batch>lambdaQuery().orderByDesc(Batch::getBatchDate));
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<BatchVO> pageWithRequirements(
            com.baomidou.mybatisplus.core.metadata.IPage<Batch> pageParam,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Batch> query) {
        com.baomidou.mybatisplus.core.metadata.IPage<Batch> page = page(pageParam, query);

        List<Long> batchIds = page.getRecords().stream()
                .map(Batch::getId)
                .toList();

        List<BizRequirement> allReqList = batchIds.isEmpty()
                ? List.of()
                : bizRequirementMapper.selectList(
                        Wrappers.<BizRequirement>lambdaQuery()
                                .in(BizRequirement::getBatchId, batchIds)
                                .orderByDesc(BizRequirement::getCreateTime));

        return page.convert(batch -> {
            BatchVO vo = new BatchVO();
            vo.setId(batch.getId());
            vo.setBatchNo(batch.getBatchNo());
            vo.setBatchType(batch.getBatchType());
            vo.setBatchDate(batch.getBatchDate());
            vo.setStatus(batch.getStatus());
            vo.setRequirements(allReqList.stream()
                    .filter(r -> r.getBatchId().equals(batch.getId()))
                    .map(r -> {
                        BatchVO.RequirementItem item = new BatchVO.RequirementItem();
                        item.setId(r.getId());
                        item.setReqCode(r.getReqCode());
                        item.setReqName(r.getReqName());
                        return item;
                    }).toList());
            return vo;
        });
    }
}
