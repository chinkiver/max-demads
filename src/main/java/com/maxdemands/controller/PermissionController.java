package com.maxdemands.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.maxdemands.entity.Permission;
import com.maxdemands.mapper.PermissionMapper;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限控制器
 */
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionMapper permissionMapper;

    @GetMapping
    public Result<List<Permission>> list() {
        List<Permission> list = permissionMapper.selectList(
                Wrappers.<Permission>lambdaQuery()
                        .orderByAsc(Permission::getModule, Permission::getId)
        );
        return Result.success(list);
    }
}
