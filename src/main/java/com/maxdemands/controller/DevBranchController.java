package com.maxdemands.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxdemands.entity.DevBranch;
import com.maxdemands.service.DevBranchService;
import com.maxdemands.vo.DevBranchVO;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev-branch")
@RequiredArgsConstructor
public class DevBranchController {

    private final DevBranchService devBranchService;

    @GetMapping
    @PreAuthorize("hasAuthority('dev_branch:list')")
    public Result<IPage<DevBranchVO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(devBranchService.pageWithProdRequirements(new Page<>(current, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dev_branch:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody DevBranch branch) {
        devBranchService.update(Wrappers.<DevBranch>lambdaUpdate()
                .set(DevBranch::getBranchName, branch.getBranchName())
                .set(DevBranch::getStatus, branch.getStatus())
                .set(DevBranch::getVerifyBranchId, branch.getVerifyBranchId())
                .eq(DevBranch::getId, id));
        return Result.success();
    }
}
