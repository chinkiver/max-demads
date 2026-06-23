package com.maxdemands.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxdemands.entity.VerifyBranch;
import com.maxdemands.service.VerifyBranchService;
import com.maxdemands.vo.VerifyBranchRelationTreeVO;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verify-branch")
@RequiredArgsConstructor
public class VerifyBranchController {

    private final VerifyBranchService verifyBranchService;

    @GetMapping
    @PreAuthorize("hasAuthority('verify_branch:list')")
    public Result<Page<VerifyBranch>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(verifyBranchService.page(new Page<>(current, size)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('verify_branch:add')")
    public Result<Void> add(@RequestBody VerifyBranch branch) {
        verifyBranchService.save(branch);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('verify_branch:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody VerifyBranch branch) {
        branch.setId(id);
        verifyBranchService.updateById(branch);
        return Result.success();
    }

    @GetMapping("/{id}/relation-tree")
    @PreAuthorize("hasAuthority('verify_branch:list')")
    public Result<VerifyBranchRelationTreeVO> relationTree(@PathVariable Long id) {
        return Result.success(verifyBranchService.buildRelationTree(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('verify_branch:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        verifyBranchService.removeById(id);
        return Result.success();
    }
}
