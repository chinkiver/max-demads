-- 仪表盘统计权限修复脚本
-- 用途：为 demand_assign、user 角色补授开发分支/验证分支的查询权限，
--       使这些角色的用户访问仪表盘时不再报 "无权访问该资源"
-- 执行方式：直接在当前数据库执行本脚本即可

-- 为 demand_assign 角色补授 dev_branch:list / verify_branch:list
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
         CROSS JOIN sys_permission p
WHERE r.role_code = 'demand_assign'
  AND p.perm_code IN ('dev_branch:list', 'verify_branch:list')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为 user 角色补授 dev_branch:list / verify_branch:list
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
         CROSS JOIN sys_permission p
WHERE r.role_code = 'user'
  AND p.perm_code IN ('dev_branch:list', 'verify_branch:list')
ON DUPLICATE KEY UPDATE role_id = role_id;
