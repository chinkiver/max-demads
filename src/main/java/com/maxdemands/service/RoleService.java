package com.maxdemands.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    void updateRolePermissions(Long roleId, List<Long> permissionIds);
    List<Long> getRolePermissionIds(Long roleId);
}
