package com.maxdemands.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.Dict;

import java.util.List;
import java.util.Map;

public interface DictService extends IService<Dict> {
    Map<String, List<Dict>> getAllDicts();

    /**
     * 根据字典类型和编码获取字典名称
     */
    String getDictName(String dictType, String dictCode);
}
