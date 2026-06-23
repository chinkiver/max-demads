package com.maxdemands.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class Dict extends BaseEntity {

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
