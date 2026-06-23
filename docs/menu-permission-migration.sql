-- 菜单可见权限迁移脚本
-- 在已运行的数据库上执行，无需删除表

-- 1. 新增菜单权限
INSERT INTO sys_permission (perm_code, perm_name, module) VALUES
('biz:requirement:menu', '业务需求菜单', '业务需求'),
('prod:requirement:menu', '产品需求菜单', '产品需求'),
('dev_branch:menu', '开发分支菜单', '开发分支'),
('verify_branch:menu', '验证分支菜单', '验证分支'),
('batch:menu', '投产批次菜单', '批次管理'),
('app:system:menu', '应用系统菜单', '应用系统'),
('sys:dict:menu', '数据字典菜单', '系统管理'),
('sys:user:menu', '用户管理菜单', '系统管理'),
('sys:role:menu', '角色权限菜单', '系统管理');

-- 2. 给 admin 角色分配所有菜单权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'admin'
  AND p.perm_code IN (
      'biz:requirement:menu', 'prod:requirement:menu', 'dev_branch:menu',
      'verify_branch:menu', 'batch:menu', 'app:system:menu',
      'sys:dict:menu', 'sys:user:menu', 'sys:role:menu'
  );

-- 3. 给 demand_assign 角色分配菜单权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'demand_assign'
  AND p.perm_code IN (
      'biz:requirement:menu', 'prod:requirement:menu', 'batch:menu', 'app:system:menu'
  );

-- 4. 给 user 角色分配菜单权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'user'
  AND p.perm_code IN (
      'biz:requirement:menu', 'prod:requirement:menu', 'batch:menu', 'app:system:menu'
  );
