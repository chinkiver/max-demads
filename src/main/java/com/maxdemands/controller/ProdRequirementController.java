package com.maxdemands.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxdemands.dto.ProdRequirementDTO;
import com.maxdemands.service.ProdRequirementService;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prod-requirement")
@RequiredArgsConstructor
public class ProdRequirementController {

    private final ProdRequirementService prodRequirementService;

    @GetMapping
    @PreAuthorize("hasAuthority('prod:requirement:list')")
    public Result<IPage<ProdRequirementDTO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long bizReqId,
            @RequestParam(required = false) String developer,
            @RequestParam(required = false) Boolean completedOnly) {
        return Result.success(prodRequirementService.pageWithBranches(new Page<>(current, size), bizReqId, developer, completedOnly));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('prod:requirement:add')")
    public Result<Void> add(@RequestBody ProdRequirementDTO dto) {
        prodRequirementService.createWithBranch(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('prod:requirement:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProdRequirementDTO dto) {
        dto.setId(id);
        prodRequirementService.updateWithBranch(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('prod:requirement:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        prodRequirementService.deleteWithBranch(id);
        return Result.success();
    }
}
