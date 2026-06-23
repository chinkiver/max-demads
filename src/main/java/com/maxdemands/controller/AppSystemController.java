package com.maxdemands.controller;

import com.maxdemands.entity.AppSystem;
import com.maxdemands.service.AppSystemService;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app-system")
@RequiredArgsConstructor
public class AppSystemController {

    private final AppSystemService appSystemService;

    @GetMapping
    @PreAuthorize("hasAuthority('app:system:list')")
    public Result<List<AppSystem>> list() {
        return Result.success(appSystemService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('app:system:add')")
    public Result<Void> add(@RequestBody AppSystem appSystem) {
        appSystemService.save(appSystem);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('app:system:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody AppSystem appSystem) {
        appSystem.setId(id);
        appSystemService.updateById(appSystem);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('app:system:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        appSystemService.removeById(id);
        return Result.success();
    }
}
