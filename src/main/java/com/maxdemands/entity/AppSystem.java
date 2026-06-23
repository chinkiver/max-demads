package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用系统实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_system")
public class AppSystem extends BaseSoftDeleteEntity {

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 归属业务部门
     */
    private String businessDept;

    /**
     * 系统负责人
     */
    private String owner;

    /**
     * 系统描述
     */
    private String description;
}
