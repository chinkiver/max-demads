package com.maxdemands.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxdemands.entity.Batch;
import com.maxdemands.vo.AutoGenerateResultVO;
import com.maxdemands.vo.BatchRequirementVO;
import com.maxdemands.vo.BatchVO;

import java.util.List;

public interface BatchService extends IService<Batch> {
    List<AutoGenerateResultVO> autoGenerate(String month);

    int autoUpdateCompleted();

    void updateBatch(Long id, Batch batch);

    List<BatchRequirementVO> listRequirements(Long batchId);

    com.baomidou.mybatisplus.core.metadata.IPage<BatchVO> pageWithRequirements(
            com.baomidou.mybatisplus.core.metadata.IPage<Batch> pageParam);

    com.baomidou.mybatisplus.core.metadata.IPage<BatchVO> pageWithRequirements(
            com.baomidou.mybatisplus.core.metadata.IPage<Batch> pageParam,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Batch> query);

    void deleteBatch(Long id);
}
