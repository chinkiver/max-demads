package com.maxdemands.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {
    private String token;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
}
