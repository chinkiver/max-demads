package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maxdemands.dto.ProdRequirementDTO;
import com.maxdemands.entity.AppSystem;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.entity.ProdRequirement;
import com.maxdemands.entity.VerifyBranch;
import com.maxdemands.mapper.AppSystemMapper;
import com.maxdemands.mapper.DevBranchMapper;
import com.maxdemands.mapper.ProdRequirementMapper;
import com.maxdemands.mapper.VerifyBranchMapper;
import com.maxdemands.service.ProdRequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdRequirementServiceImpl extends ServiceImpl<ProdRequirementMapper, ProdRequirement> implements ProdRequirementService {

    private final DevBranchMapper devBranchMapper;
    private final VerifyBranchMapper verifyBranchMapper;
    private final AppSystemMapper appSystemMapper;

    @Override
    public IPage<ProdRequirementDTO> pageWithBranches(IPage<ProdRequirement> pageParam, Long bizReqId) {
        var query = Wrappers.<ProdRequirement>lambdaQuery()
                .eq(bizReqId != null, ProdRequirement::getBizReqId, bizReqId)
                .orderByDesc(ProdRequirement::getCreateTime);
        IPage<ProdRequirement> page = page(pageParam, query);

        List<Long> systemIds = page.getRecords().stream()
                .map(ProdRequirement::getSystemId)
                .distinct()
                .toList();
        List<AppSystem> systemList = systemIds.isEmpty()
                ? List.of()
                : appSystemMapper.selectList(
                        Wrappers.<AppSystem>lambdaQuery()
                                .in(AppSystem::getId, systemIds));

        return page.convert(prod -> {
            ProdRequirementDTO dto = new ProdRequirementDTO();
            dto.setId(prod.getId());
            dto.setProdReqCode(prod.getProdReqCode());
            dto.setProdReqName(prod.getProdReqName());
            dto.setSummary(prod.getSummary());
            dto.setSystemId(prod.getSystemId());
            AppSystem appSystem = systemList.stream()
                    .filter(s -> s.getId().equals(prod.getSystemId()))
                    .findFirst()
                    .orElse(null);
            if (appSystem != null) {
                dto.setSystemName(appSystem.getSystemName());
            }
            dto.setDeveloper(prod.getDeveloper());
            dto.setStatus(prod.getStatus());
            dto.setBizReqId(prod.getBizReqId());
            dto.setBranchAction(prod.getBranchAction());
            dto.setDevBranchId(prod.getDevBranchId());

            Long devBranchId = prod.getDevBranchId();
            if (devBranchId != null) {
                DevBranch devBranch = devBranchMapper.selectById(devBranchId);
                if (devBranch != null) {
                    dto.setDevBranchName(devBranch.getBranchName());
                    dto.setDevBranchStatus(devBranch.getStatus());

                    Long verifyBranchId = devBranch.getVerifyBranchId();
                    if (verifyBranchId != null) {
                        VerifyBranch verifyBranch = verifyBranchMapper.selectById(verifyBranchId);
                        if (verifyBranch != null) {
                            dto.setVerifyBranchName(verifyBranch.getBranchName());
                            dto.setVerifyBranchStatus(verifyBranch.getStatus());
                        }
                    }
                }
            }
            return dto;
        });
    }

    @Override
    @Transactional
    public void createWithBranch(ProdRequirementDTO dto) {
        ProdRequirement prod = new ProdRequirement();
        prod.setProdReqCode(dto.getProdReqCode());
        prod.setProdReqName(dto.getProdReqName());
        prod.setSummary(dto.getSummary());
        prod.setSystemId(dto.getSystemId());
        prod.setDeveloper(dto.getDeveloper());
        prod.setStatus(dto.getStatus());
        prod.setBizReqId(dto.getBizReqId());
        prod.setBranchAction(dto.getBranchAction());

        if ("create".equals(dto.getBranchAction())) {
            DevBranch branch = new DevBranch();
            branch.setBranchName("DEV_" + dto.getProdReqCode());
            branch.setSystemId(dto.getSystemId());
            branch.setStatus("active");
            devBranchMapper.insert(branch);
            prod.setDevBranchId(branch.getId());
        } else if ("associate".equals(dto.getBranchAction())) {
            prod.setDevBranchId(dto.getDevBranchId());
        }

        save(prod);
    }

    @Override
    @Transactional
    public void updateWithBranch(ProdRequirementDTO dto) {
        ProdRequirement existing = getById(dto.getId());
        if (existing == null) return;

        existing.setProdReqCode(dto.getProdReqCode());
        existing.setProdReqName(dto.getProdReqName());
        existing.setSummary(dto.getSummary());
        existing.setSystemId(dto.getSystemId());
        existing.setDeveloper(dto.getDeveloper());
        existing.setStatus(dto.getStatus());
        existing.setBizReqId(dto.getBizReqId());
        existing.setBranchAction(dto.getBranchAction());

        if ("create".equals(dto.getBranchAction()) && existing.getDevBranchId() == null) {
            DevBranch branch = new DevBranch();
            branch.setBranchName("DEV_" + dto.getProdReqCode());
            branch.setSystemId(dto.getSystemId());
            branch.setStatus("active");
            devBranchMapper.insert(branch);
            existing.setDevBranchId(branch.getId());
        } else if ("associate".equals(dto.getBranchAction())) {
            existing.setDevBranchId(dto.getDevBranchId());
        } else if ("none".equals(dto.getBranchAction())) {
            existing.setDevBranchId(null);
        }

        updateById(existing);
    }

    @Override
    @Transactional
    public void deleteWithBranch(Long id) {
        ProdRequirement prod = getById(id);
        if (prod == null) return;

        Long branchId = prod.getDevBranchId();
        if (branchId != null) {
            long count = count(Wrappers.<ProdRequirement>lambdaQuery()
                    .eq(ProdRequirement::getDevBranchId, branchId)
                    .ne(ProdRequirement::getId, id));

            if (count == 0) {
                devBranchMapper.deleteById(branchId);
            }
        }

        removeById(id);
    }
}
