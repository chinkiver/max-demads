-- 应用系统 system_name 唯一性迁移脚本
-- 用途：为现有 app_system 表增加唯一索引，并清理重复记录

-- 1. 先清理重复的系统名称，只保留 id 最小的记录
DELETE t1 FROM app_system t1
INNER JOIN app_system t2
WHERE t1.id > t2.id AND t1.system_name = t2.system_name;

-- 2. 添加唯一索引
ALTER TABLE app_system ADD UNIQUE KEY uk_system_name (system_name);
