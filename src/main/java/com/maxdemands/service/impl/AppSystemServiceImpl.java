package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.common.exception.BusinessException;
import com.maxdemands.entity.AppSystem;
import com.maxdemands.mapper.AppSystemMapper;
import com.maxdemands.service.AppSystemService;
import org.springframework.stereotype.Service;

@Service
public class AppSystemServiceImpl extends ServiceImpl<AppSystemMapper, AppSystem> implements AppSystemService {

    @Override
    public boolean save(AppSystem entity) {
        if (existsBySystemName(entity.getSystemName(), null)) {
            throw new BusinessException("系统名称已存在：" + entity.getSystemName());
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(AppSystem entity) {
        if (existsBySystemName(entity.getSystemName(), entity.getId())) {
            throw new BusinessException("系统名称已存在：" + entity.getSystemName());
        }
        return super.updateById(entity);
    }

    private boolean existsBySystemName(String systemName, Long excludeId) {
        if (systemName == null || systemName.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<AppSystem> wrapper = new LambdaQueryWrapper<AppSystem>()
                .eq(AppSystem::getSystemName, systemName.trim());
        if (excludeId != null) {
            wrapper.ne(AppSystem::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
