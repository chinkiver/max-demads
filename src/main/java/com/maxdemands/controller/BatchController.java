package com.maxdemands.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxdemands.dto.AutoGenerateBatchDTO;
import com.maxdemands.entity.Batch;
import com.maxdemands.service.BatchService;
import com.maxdemands.service.DictService;
import com.maxdemands.vo.AutoGenerateResultVO;
import com.maxdemands.vo.BatchRequirementVO;
import com.maxdemands.vo.BatchVO;
import com.maxdemands.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final DictService dictService;

    @GetMapping
    @PreAuthorize("hasAuthority('batch:list')")
    public Result<IPage<BatchVO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean availableOnly) {
        var query = Wrappers.<Batch>lambdaQuery()
                .ge(availableOnly != null && availableOnly, Batch::getBatchDate, LocalDate.now())
                .orderByDesc(Batch::getBatchDate);
        return Result.success(batchService.pageWithRequirements(new Page<>(current, size), query));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('batch:add')")
    public Result<Void> add(@RequestBody Batch batch) {
        String typeName = dictService.getDictName("batch_type", batch.getBatchType());
        batch.setBatchNo(batch.getBatchDate().toString().replace("-", "") + "-" + typeName);
        batchService.save(batch);
        return Result.success();
    }

    @PostMapping("/auto-generate")
    @PreAuthorize("hasAuthority('batch:edit')")
    public Result<List<AutoGenerateResultVO>> autoGenerate(@RequestBody AutoGenerateBatchDTO dto) {
        return Result.success(batchService.autoGenerate(dto.getMonth()));
    }

    @PostMapping("/auto-update")
    @PreAuthorize("hasAuthority('batch:edit')")
    public Result<Integer> autoUpdate() {
        int count = batchService.autoUpdateCompleted();
        return Result.success(count);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('batch:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Batch batch) {
        batchService.updateBatch(id, batch);
        return Result.success();
    }

    @GetMapping("/{id}/requirements")
    @PreAuthorize("hasAuthority('batch:list')")
    public Result<List<BatchRequirementVO>> listRequirements(@PathVariable Long id) {
        return Result.success(batchService.listRequirements(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('batch:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return Result.success();
    }
}
