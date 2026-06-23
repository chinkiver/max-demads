package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带逻辑删除的实体基类
 * 业务表继承此基类，系统表继承 BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseSoftDeleteEntity extends BaseEntity {

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
