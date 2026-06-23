package com.maxdemands.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolePermissionDTO {
    private List<Long> permissionIds;
}
