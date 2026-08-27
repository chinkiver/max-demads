package com.maxdemands.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxdemands.entity.BizRequirement;
import com.maxdemands.entity.ProdRequirement;
import com.maxdemands.service.BizRequirementService;
import com.maxdemands.service.ProdRequirementService;
import com.maxdemands.vo.BizRequirementOverviewVO;
import com.maxdemands.vo.BizRequirementVO;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/biz-requirement")
@RequiredArgsConstructor
public class BizRequirementController {

    private final BizRequirementService bizRequirementService;
    private final ProdRequirementService prodRequirementService;

    @GetMapping
    @PreAuthorize("hasAuthority('biz:requirement:list')")
    public Result<IPage<BizRequirementVO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String reqCode,
            @RequestParam(required = false) String reqName) {
        return Result.success(bizRequirementService.pageWithBatch(new Page<>(current, size), status, batchId, owner, reqCode, reqName));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('biz:requirement:list')")
    public Result<BizRequirement> getById(@PathVariable Long id) {
        return Result.success(bizRequirementService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('biz:requirement:add')")
    public Result<Void> add(@RequestBody BizRequirement requirement) {
        bizRequirementService.save(requirement);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('biz:requirement:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody BizRequirement requirement) {
        requirement.setId(id);
        bizRequirementService.updateById(requirement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('biz:requirement:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        bizRequirementService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}/prod-requirements")
    @PreAuthorize("hasAuthority('prod:requirement:list')")
    public Result<List<ProdRequirement>> getProdRequirements(@PathVariable Long id) {
        List<ProdRequirement> list = prodRequirementService.list(
                Wrappers.<ProdRequirement>lambdaQuery()
                        .eq(ProdRequirement::getBizReqId, id)
        );
        return Result.success(list);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('biz:requirement:overview:list')")
    public Result<List<BizRequirementOverviewVO>> overview() {
        return Result.success(bizRequirementService.buildOverview());
    }

    @GetMapping("/overview/completed")
    @PreAuthorize("hasAuthority('biz:requirement:completed:list')")
    public Result<List<BizRequirementOverviewVO>> overviewCompleted() {
        return Result.success(bizRequirementService.buildOverviewCompleted());
    }

    @PutMapping("/{id}/production-date")
    @PreAuthorize("hasAuthority('biz:requirement:edit')")
    public Result<Void> updateProductionDate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String productionDate = body.get("productionDate");
        BizRequirement requirement = new BizRequirement();
        requirement.setId(id);
        requirement.setProductionDate(productionDate != null && !productionDate.isEmpty()
                ? java.time.LocalDate.parse(productionDate) : null);
        bizRequirementService.updateById(requirement);
        return Result.success();
    }

    @GetMapping("/count-by-status")
    @PreAuthorize("hasAuthority('biz:requirement:list')")
    public Result<List<Map<String, Object>>> countByStatus() {
        return Result.success(bizRequirementService.countByStatus());
    }
}
