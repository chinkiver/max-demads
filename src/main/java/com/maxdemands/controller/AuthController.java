package com.maxdemands.controller;

import com.maxdemands.dto.LoginDTO;
import com.maxdemands.entity.User;
import com.maxdemands.mapper.UserMapper;
import com.maxdemands.util.JwtTokenProvider;
import com.maxdemands.service.UserService;
import com.maxdemands.vo.LoginVO;
import com.maxdemands.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        User user = userService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginDTO.getUsername())
        );

        String token = jwtTokenProvider.generateToken(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoles(userMapper.selectRolesByUserId(user.getId()).stream().map(r -> r.getRoleCode()).toList());
        vo.setPermissions(userMapper.selectPermissionsByUserId(user.getId()).stream().map(p -> p.getPermCode()).toList());

        return Result.success(vo);
    }

    @GetMapping("/info")
    public Result<LoginVO> info(@RequestAttribute("userId") Long userId) {
        User user = userService.getById(userId);
        LoginVO vo = new LoginVO();
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoles(userMapper.selectRolesByUserId(userId).stream().map(r -> r.getRoleCode()).toList());
        vo.setPermissions(userMapper.selectPermissionsByUserId(userId).stream().map(p -> p.getPermCode()).toList());
        return Result.success(vo);
    }
}
