package com.maxdemands.controller;

import com.maxdemands.entity.Dict;
import com.maxdemands.service.DictService;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/{dictType}")
    public Result<List<Dict>> getByType(@PathVariable String dictType) {
        List<Dict> list = dictService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dict>()
                        .eq(Dict::getDictType, dictType)
                        .orderByAsc(Dict::getSortOrder)
        );
        return Result.success(list);
    }

    @GetMapping("/types")
    public Result<Map<String, List<Dict>>> getAllTypes() {
        return Result.success(dictService.getAllDicts());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    public Result<Void> add(@RequestBody Dict dict) {
        dictService.save(dict);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.removeById(id);
        return Result.success();
    }
}
