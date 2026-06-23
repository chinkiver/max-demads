package com.maxdemands.security;

import com.maxdemands.entity.Permission;
import com.maxdemands.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务
 * 提供Spring Security SpEL表达式中使用的权限判断方法
 * 在注解中通过 @ss.hasPermi('perm:code') 调用
 */
@Slf4j
@Service("ss")
@RequiredArgsConstructor
public class PermissionService {

    private final UserMapper userMapper;

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param permission 权限编码
     * @return 是否拥有该权限
     */
    public boolean hasPermi(String permission) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        List<String> permCodes = getUserPermissions(userId);
        boolean hasPerm = permCodes.contains(permission);
        log.debug("用户[{}]权限检查[{}]: {}", userId, permission, hasPerm);
        return hasPerm;
    }

    /**
     * 获取用户权限编码列表（带缓存）
     */
    @Cacheable(value = "user-permissions", key = "#userId")
    public List<String> getUserPermissions(Long userId) {
        List<Permission> permissions = userMapper.selectPermissionsByUserId(userId);
        return permissions.stream()
                .map(Permission::getPermCode)
                .collect(Collectors.toList());
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param role 角色编码
     * @return 是否拥有该角色
     */
    public boolean hasRole(String role) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        List<String> roleCodes = getUserRoles(userId);
        boolean hasRole = roleCodes.contains(role);
        log.debug("用户[{}]角色检查[{}]: {}", userId, role, hasRole);
        return hasRole;
    }

    /**
     * 获取用户角色编码列表（带缓存）
     */
    @Cacheable(value = "user-roles", key = "#userId")
    public List<String> getUserRoles(Long userId) {
        var roles = userMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(r -> r.getRoleCode())
                .collect(Collectors.toList());
    }

    /**
     * 清除用户权限缓存（角色或权限变更时调用）
     */
    @CacheEvict(value = {"user-permissions", "user-roles"}, allEntries = true)
    public void clearUserCache() {
        log.debug("清除用户权限缓存");
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            log.warn("无法解析当前用户ID: {}", authentication.getName());
            return null;
        }
    }
}
