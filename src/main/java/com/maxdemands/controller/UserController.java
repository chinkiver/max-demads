package com.maxdemands.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxdemands.annotation.OperationLog;
import com.maxdemands.dto.UserDTO;
import com.maxdemands.entity.User;
import com.maxdemands.entity.UserRole;
import com.maxdemands.mapper.UserRoleMapper;
import com.maxdemands.service.UserService;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<Page<User>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.page(new Page<>(current, size)));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<List<UserRole>> listAllUserRoles() {
        return Result.success(userRoleMapper.selectList(null));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:add')")
    @OperationLog(value = "新增用户", module = "系统管理")
    public Result<Void> add(@RequestBody UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setStatus(dto.getStatus());
        userService.save(user);

        if (dto.getRoleIds() != null) {
            for (Long roleId : dto.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog(value = "修改用户状态", module = "系统管理")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User existing = userService.getById(id);
        if (existing != null && "admin".equals(existing.getUsername())) {
            throw new com.maxdemands.common.exception.BusinessException("系统管理员账号不允许禁用或启用");
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog(value = "重置用户密码", module = "系统管理")
    public Result<Void> resetPassword(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<UserRole> list = userRoleMapper.selectList(
                Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, id)
        );
        return Result.success(list.stream().map(UserRole::getRoleId).toList());
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog(value = "分配用户角色", module = "系统管理")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        User existing = userService.getById(id);
        if (existing != null && "admin".equals(existing.getUsername())) {
            throw new com.maxdemands.common.exception.BusinessException("系统管理员账号不允许重新分配角色");
        }
        // 物理删除旧的角色关联，避免逻辑删除导致的唯一索引冲突
        userRoleMapper.deletePhysicalByUserId(id);

        if (roleIds != null) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }
}
