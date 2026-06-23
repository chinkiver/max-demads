package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.Dict;
import com.maxdemands.mapper.DictMapper;
import com.maxdemands.service.DictService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Override
    public Map<String, List<Dict>> getAllDicts() {
        List<Dict> list = list(Wrappers.<Dict>lambdaQuery().orderByAsc(Dict::getSortOrder));
        return list.stream().collect(Collectors.groupingBy(Dict::getDictType));
    }

    @Override
    public String getDictName(String dictType, String dictCode) {
        Dict dict = getOne(Wrappers.<Dict>lambdaQuery()
                .eq(Dict::getDictType, dictType)
                .eq(Dict::getDictCode, dictCode));
        return dict != null ? dict.getDictName() : dictCode;
    }
}
