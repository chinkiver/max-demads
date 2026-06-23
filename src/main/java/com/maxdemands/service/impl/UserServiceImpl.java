package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.User;
import com.maxdemands.mapper.UserMapper;
import com.maxdemands.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
