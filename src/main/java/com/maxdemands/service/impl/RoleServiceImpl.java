package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.RolePermission;
import com.maxdemands.mapper.RoleMapper;
import com.maxdemands.mapper.RolePermissionMapper;
import com.maxdemands.entity.Role;
import com.maxdemands.security.PermissionService;
import com.maxdemands.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        permissionService.clearUserCache();
        rolePermissionMapper.deletePhysicalByRoleId(roleId);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long pid : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        List<RolePermission> list = rolePermissionMapper.selectList(
                Wrappers.<RolePermission>lambdaQuery()
                        .eq(RolePermission::getRoleId, roleId)
        );
        return list.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
    }
}
