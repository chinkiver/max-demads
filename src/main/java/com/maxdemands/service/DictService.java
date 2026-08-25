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

    /**
     * 随机分配指定字典类型下所有子项的颜色（组内无重复）
     *
     * @return 受影响的子项数量
     */
    int randomColorsByType(String dictType);

    /**
     * 随机分配所有字典类型下每个子项的颜色（每个 dictType 内无重复）
     *
     * @return key=dictType, value=该类型下受影响的子项数量
     */
    Map<String, Integer> randomColorsAll();
}
