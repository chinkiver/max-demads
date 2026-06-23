package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.AppSystem;
import com.maxdemands.mapper.AppSystemMapper;
import com.maxdemands.service.AppSystemService;
import org.springframework.stereotype.Service;

@Service
public class AppSystemServiceImpl extends ServiceImpl<AppSystemMapper, AppSystem> implements AppSystemService {
}
