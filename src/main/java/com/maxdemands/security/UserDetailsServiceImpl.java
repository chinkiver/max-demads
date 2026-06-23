package com.maxdemands.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maxdemands.entity.Permission;
import com.maxdemands.entity.Role;
import com.maxdemands.entity.User;
import com.maxdemands.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 * 从数据库加载用户信息及权限，供Spring Security认证使用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试将用户名解析为用户ID
        Long userId;
        try {
            userId = Long.valueOf(username);
        } catch (NumberFormatException e) {
            // 如果不是数字，按用户名查询
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username);
            User user = userMapper.selectOne(wrapper);
            if (user == null) {
                log.warn("用户不存在: {}", username);
                throw new UsernameNotFoundException("用户不存在: " + username);
            }
            userId = user.getId();
        }

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询用户角色
        List<Role> roles = userMapper.selectRolesByUserId(userId);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
                .collect(Collectors.toList());

        // 查询用户权限
        List<Permission> permissions = userMapper.selectPermissionsByUserId(userId);
        authorities.addAll(permissions.stream()
                .map(perm -> new SimpleGrantedAuthority(perm.getPermCode()))
                .collect(Collectors.toList()));

        log.debug("加载用户[{}]的权限: 角色数={}, 权限数={}", username, roles.size(), permissions.size());

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId()))
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(user.getStatus() != null && user.getStatus() == 0)
                .build();
    }
}
