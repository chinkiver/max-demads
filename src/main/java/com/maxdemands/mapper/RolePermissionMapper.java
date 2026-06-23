package com.maxdemands.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maxdemands.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 物理删除指定角色的权限关联（绕过逻辑删除）
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deletePhysicalByRoleId(@Param("roleId") Long roleId);
}
