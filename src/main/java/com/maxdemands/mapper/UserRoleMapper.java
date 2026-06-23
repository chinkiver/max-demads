package com.maxdemands.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maxdemands.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联Mapper接口
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 物理删除指定用户的角色关联（绕过逻辑删除）
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deletePhysicalByUserId(@Param("userId") Long userId);
}
