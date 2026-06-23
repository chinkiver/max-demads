-- 业务需求拆分为需求管理、需求全览、已投产需求三个子菜单的权限迁移脚本
-- 用途：为现有数据库补充需求全览、已投产需求相关权限，并授予已有业务需求菜单权限的角色

-- 1. 新增需求全览/已投产需求权限
INSERT INTO sys_permission (perm_code, perm_name, module) VALUES
('biz:requirement:overview:list', '需求全览列表', '业务需求'),
('biz:requirement:overview:menu', '需求全览菜单', '业务需求'),
('biz:requirement:completed:list', '已投产需求列表', '业务需求'),
('biz:requirement:completed:menu', '已投产需求菜单', '业务需求')
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 2. 将权限授予 admin 角色（admin 拥有全部权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'admin'
  AND p.perm_code IN (
      'biz:requirement:overview:list', 'biz:requirement:overview:menu',
      'biz:requirement:completed:list', 'biz:requirement:completed:menu'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 3. 将权限授予 demand_assign 角色
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'demand_assign'
  AND p.perm_code IN (
      'biz:requirement:overview:list', 'biz:requirement:overview:menu',
      'biz:requirement:completed:list', 'biz:requirement:completed:menu'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 4. 将权限授予 user 角色
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'user'
  AND p.perm_code IN (
      'biz:requirement:overview:list', 'biz:requirement:overview:menu',
      'biz:requirement:completed:list', 'biz:requirement:completed:menu'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;
