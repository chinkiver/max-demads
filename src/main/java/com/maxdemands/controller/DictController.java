package com.maxdemands.controller;

import com.maxdemands.entity.Dict;
import com.maxdemands.service.DictService;
import com.maxdemands.common.result.Result;
import com.maxdemands.annotation.OperationLog;
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
    @PreAuthorize("hasAuthority('sys:dict:add')")
    public Result<Void> add(@RequestBody Dict dict) {
        dictService.save(dict);
        return Result.success();
    }

    @PutMapping("/{dictType}/{dictCode}")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    public Result<Void> update(@PathVariable String dictType,
                               @PathVariable String dictCode,
                               @RequestBody Dict dict) {
        dictService.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Dict>()
                .set(Dict::getDictName, dict.getDictName())
                .set(Dict::getSortOrder, dict.getSortOrder())
                .set(Dict::getColor, dict.getColor())
                .eq(Dict::getDictType, dictType)
                .eq(Dict::getDictCode, dictCode));
        return Result.success();
    }

    @DeleteMapping("/{dictType}/{dictCode}")
    @PreAuthorize("hasAuthority('sys:dict:delete')")
    public Result<Void> delete(@PathVariable String dictType, @PathVariable String dictCode) {
        dictService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dict>()
                .eq(Dict::getDictType, dictType)
                .eq(Dict::getDictCode, dictCode));
        return Result.success();
    }

    /**
     * 随机分配指定字典类型下所有子项的颜色（组内无重复）
     */
    @PutMapping("/{dictType}/random-colors")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @OperationLog(value = "随机分配字典颜色", module = "字典管理")
    public Result<Integer> randomColorsByType(@PathVariable String dictType) {
        return Result.success(dictService.randomColorsByType(dictType));
    }

    /**
     * 随机分配所有字典类型下每个子项的颜色（每个 dictType 内无重复）
     */
    @PutMapping("/random-colors")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @OperationLog(value = "一键随机所有字典颜色", module = "字典管理")
    public Result<Map<String, Integer>> randomColorsAll() {
        return Result.success(dictService.randomColorsAll());
    }
}
