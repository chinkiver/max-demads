package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.Batch;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.mapper.BatchMapper;
import com.maxdemands.mapper.BizRequirementMapper;
import com.maxdemands.service.BizRequirementService;
import com.maxdemands.vo.BizRequirementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizRequirementServiceImpl extends ServiceImpl<BizRequirementMapper, BizRequirement> implements BizRequirementService {

    private final BatchMapper batchMapper;

    @Override
    public IPage<BizRequirementVO> pageWithBatch(IPage<BizRequirement> pageParam, String status, Long batchId) {
        var query = Wrappers.<BizRequirement>lambdaQuery()
                .eq(status != null && !status.isEmpty(), BizRequirement::getStatus, status)
                .eq(batchId != null, BizRequirement::getBatchId, batchId)
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
}
