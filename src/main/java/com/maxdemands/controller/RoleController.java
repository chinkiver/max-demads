package com.maxdemands.controller;

import com.maxdemands.annotation.OperationLog;
import com.maxdemands.dto.RolePermissionDTO;
import com.maxdemands.entity.Role;
import com.maxdemands.service.RoleService;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<List<Role>> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog(value = "配置角色权限", module = "系统管理")
    public Result<Void> updatePermissions(@PathVariable Long id, @RequestBody RolePermissionDTO dto) {
        roleService.updateRolePermissions(id, dto.getPermissionIds());
        return Result.success();
    }
}
