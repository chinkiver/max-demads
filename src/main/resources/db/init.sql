-- =====================================================
-- 需求管理平台数据库初始化脚本
-- 数据库: max_demands
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS max_demands
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE max_demands;

-- =====================================================
-- 1. 系统管理相关表
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    module VARCHAR(50) COMMENT '所属模块',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 字典表
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- =====================================================
-- 2. 业务相关表
-- =====================================================

-- 应用系统表
CREATE TABLE IF NOT EXISTS app_system (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    system_name VARCHAR(100) NOT NULL COMMENT '系统名称',
    business_dept VARCHAR(100) NOT NULL COMMENT '归属业务部门',
    owner VARCHAR(50) COMMENT '系统负责人',
    description VARCHAR(500) COMMENT '系统描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用系统表';

-- 批次表
CREATE TABLE IF NOT EXISTS batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次编号',
    batch_type VARCHAR(20) COMMENT '批次类型',
    batch_date DATE COMMENT '批次日期',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次表';

-- 业务需求表
CREATE TABLE IF NOT EXISTS biz_requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    req_code VARCHAR(50) NOT NULL COMMENT '需求编号',
    req_name VARCHAR(200) NOT NULL COMMENT '需求名称',
    req_category VARCHAR(50) COMMENT '需求分类',
    summary TEXT COMMENT '需求摘要',
    priority VARCHAR(20) COMMENT '优先级',
    proposer VARCHAR(50) COMMENT '提出人',
    proposer_dept VARCHAR(100) COMMENT '提出部门',
    owner VARCHAR(50) COMMENT '负责人',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    batch_id BIGINT COMMENT '所属批次ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务需求表';

-- 产品需求表
CREATE TABLE IF NOT EXISTS prod_requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    prod_req_code VARCHAR(50) NOT NULL COMMENT '产品需求编号',
    prod_req_name VARCHAR(200) NOT NULL COMMENT '产品需求名称',
    summary TEXT COMMENT '需求摘要',
    system_id BIGINT COMMENT '所属系统ID',
    developer VARCHAR(50) COMMENT '开发人员',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    biz_req_id BIGINT COMMENT '关联业务需求ID',
    dev_branch_id BIGINT COMMENT '开发分支ID',
    branch_action VARCHAR(20) COMMENT '分支操作类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品需求表';

-- 开发分支表
CREATE TABLE IF NOT EXISTS dev_branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    branch_name VARCHAR(100) NOT NULL COMMENT '分支名称',
    system_id BIGINT COMMENT '所属系统ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    verify_branch_id BIGINT COMMENT '验证分支ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开发分支表';

-- 验证分支表
CREATE TABLE IF NOT EXISTS verify_branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    branch_name VARCHAR(100) NOT NULL COMMENT '分支名称',
    system_id BIGINT COMMENT '所属系统ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    batch_id BIGINT COMMENT '所属批次ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证分支表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    status TINYINT DEFAULT 1 COMMENT '操作结果：0-失败，1-成功',
    ip VARCHAR(50) COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================
-- 3. 初始化数据
-- =====================================================

-- 初始化角色
INSERT INTO sys_role (role_code, role_name, description) VALUES
('admin', '系统管理员', '拥有所有权限'),
('demand_assign', '需求分配员', '负责需求分配和管理'),
('user', '普通用户', '普通业务用户')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 初始化权限（约28个权限点）
INSERT INTO sys_permission (perm_code, perm_name, module) VALUES
-- 系统管理
('sys:user:list', '用户列表', '系统管理'),
('sys:user:add', '新增用户', '系统管理'),
('sys:user:edit', '编辑用户', '系统管理'),
('sys:user:delete', '删除用户', '系统管理'),
('sys:role:list', '角色列表', '系统管理'),
('sys:role:add', '新增角色', '系统管理'),
('sys:role:edit', '编辑角色', '系统管理'),
('sys:role:delete', '删除角色', '系统管理'),
('sys:permission:list', '权限列表', '系统管理'),
('sys:dict:list', '字典列表', '系统管理'),
('sys:dict:add', '新增字典', '系统管理'),
('sys:dict:edit', '编辑字典', '系统管理'),
('sys:dict:delete', '删除字典', '系统管理'),
-- 应用系统
('app:system:list', '系统列表', '应用系统'),
('app:system:add', '新增系统', '应用系统'),
('app:system:edit', '编辑系统', '应用系统'),
('app:system:delete', '删除系统', '应用系统'),
-- 批次管理
('batch:list', '批次列表', '批次管理'),
('batch:add', '新增批次', '批次管理'),
('batch:edit', '编辑批次', '批次管理'),
('batch:delete', '删除批次', '批次管理'),
-- 业务需求
('biz:requirement:list', '业务需求列表', '业务需求'),
('biz:requirement:add', '新增业务需求', '业务需求'),
('biz:requirement:edit', '编辑业务需求', '业务需求'),
('biz:requirement:delete', '删除业务需求', '业务需求'),
('biz:requirement:overview:list', '需求全览列表', '业务需求'),
('biz:requirement:completed:list', '已投产需求列表', '业务需求'),
-- 产品需求
('prod:requirement:list', '产品需求列表', '产品需求'),
('prod:requirement:add', '新增产品需求', '产品需求'),
('prod:requirement:edit', '编辑产品需求', '产品需求'),
('prod:requirement:delete', '删除产品需求', '产品需求'),
-- 开发分支
('dev_branch:list', '开发分支列表', '开发分支'),
('dev_branch:edit', '编辑开发分支', '开发分支'),
-- 验证分支
('verify_branch:list', '验证分支列表', '验证分支'),
('verify_branch:add', '新增验证分支', '验证分支'),
('verify_branch:edit', '编辑验证分支', '验证分支'),
('verify_branch:delete', '删除验证分支', '验证分支')
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 菜单可见权限
INSERT INTO sys_permission (perm_code, perm_name, module) VALUES
('biz:requirement:menu', '业务需求菜单', '业务需求'),
('biz:requirement:overview:menu', '需求全览菜单', '业务需求'),
('biz:requirement:completed:menu', '已投产需求菜单', '业务需求'),
('prod:requirement:menu', '产品需求菜单', '产品需求'),
('dev_branch:menu', '开发分支菜单', '开发分支'),
('verify_branch:menu', '验证分支菜单', '验证分支'),
('batch:menu', '投产批次菜单', '批次管理'),
('app:system:menu', '应用系统菜单', '应用系统'),
('sys:dict:menu', '数据字典菜单', '系统管理'),
('sys:user:menu', '用户管理菜单', '系统管理'),
('sys:role:menu', '角色权限菜单', '系统管理')
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 分配角色权限
-- admin角色拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- demand_assign角色拥有需求相关权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'demand_assign'
  AND p.perm_code IN (
      'biz:requirement:menu', 'biz:requirement:list', 'biz:requirement:add', 'biz:requirement:edit',
      'biz:requirement:overview:menu', 'biz:requirement:overview:list',
      'biz:requirement:completed:menu', 'biz:requirement:completed:list',
      'prod:requirement:menu', 'prod:requirement:list', 'prod:requirement:add', 'prod:requirement:edit',
      'dev_branch:menu', 'dev_branch:list',
      'verify_branch:menu', 'verify_branch:list',
      'batch:menu', 'batch:list', 'batch:add', 'batch:edit', 'batch:delete',
      'app:system:menu', 'app:system:list'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- user角色拥有查看权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'user'
  AND p.perm_code IN (
      'biz:requirement:menu', 'biz:requirement:list',
      'biz:requirement:overview:menu', 'biz:requirement:overview:list',
      'biz:requirement:completed:menu', 'biz:requirement:completed:list',
      'prod:requirement:menu', 'prod:requirement:list',
      'dev_branch:menu', 'dev_branch:list',
      'verify_branch:menu', 'verify_branch:list',
      'batch:menu', 'batch:list',
      'app:system:menu', 'app:system:list'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 初始化默认管理员账号：admin/admin123
-- BCrypt密码：$2a$10$y8Q3rvzUn9uqc3DLzbovYe82Ycd55w9TLSQnpqrT05nGMSYfUW4Xy
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$y8Q3rvzUn9uqc3DLzbovYe82Ycd55w9TLSQnpqrT05nGMSYfUW4Xy', '系统管理员', 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);

-- 更新admin密码为BCrypt加密的 admin123
UPDATE sys_user SET password = '$2a$10$y8Q3rvzUn9uqc3DLzbovYe82Ycd55w9TLSQnpqrT05nGMSYfUW4Xy' WHERE username = 'admin';

-- 分配admin角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'admin'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- =====================================================
-- 4. 初始化字典数据
-- =====================================================

-- 需求分类
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('req_category', 'requirement', '业务需求', 1),
('req_category', 'production_issue', '生产问题', 2),
('req_category', 'self_optimization', '自主优化', 3)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 优先级
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('priority', 'urgent', '紧急', 1),
('priority', 'high', '高', 2),
('priority', 'medium', '中', 3),
('priority', 'low', '低', 4)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 业务需求状态
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('biz_req_status', 'draft', '草稿', 1),
('biz_req_status', 'pending', '待分配', 2),
('biz_req_status', 'assigned', '已分配', 3),
('biz_req_status', 'in_progress', '进行中', 4),
('biz_req_status', 'system_testing', '系统测试中', 5),
('biz_req_status', 'acceptance_testing', '验收测试中', 6),
('biz_req_status', 'pending_production', '待投产', 7),
('biz_req_status', 'completed', '已完成', 8),
('biz_req_status', 'cancelled', '已取消', 9)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 产品需求状态
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('prod_req_status', 'draft', '草稿', 1),
('prod_req_status', 'pending', '待开发', 2),
('prod_req_status', 'developing', '开发中', 3),
('prod_req_status', 'testing', '测试中', 4),
('prod_req_status', 'completed', '已完成', 5),
('prod_req_status', 'cancelled', '已取消', 6)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 分支状态
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('branch_status', 'active', '活跃', 1),
('branch_status', 'merged', '已合并', 2),
('branch_status', 'closed', '已关闭', 3)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 批次类型
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('batch_type', 'standard_production', '标准投产', 1),
('batch_type', 'routine_production', '常规投产', 2),
('batch_type', 'emergency_production', '紧急投产', 3),
('batch_type', 'special_production', '特批投产', 4)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 批次状态
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order) VALUES
('batch_status', 'draft', '草稿', 1),
('batch_status', 'planning', '规划中', 2),
('batch_status', 'in_progress', '进行中', 3),
('batch_status', 'completed', '已完成', 4),
('batch_status', 'cancelled', '已取消', 5)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);
