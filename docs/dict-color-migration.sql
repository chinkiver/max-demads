-- =====================================================
-- 字典颜色标签迁移脚本
-- 适用版本：max-demands 1.0.2+
-- 作用：sys_dict 加 color 列 + 历史数据回填
-- 兼容：MySQL 5.6 / 5.7 / 8.0+
-- =====================================================

-- 1) 加 color 列（如已存在会报错，可忽略；建议运维先确认列是否存在）
ALTER TABLE sys_dict
    ADD COLUMN color VARCHAR(7) DEFAULT NULL COMMENT '颜色标签 #RRGGBB';

-- 2) 历史数据回填：按 (dict_type, sort_order, id) 分组排序，循环取 24 色调色板
--    使用会话变量模拟 ROW_NUMBER，兼容 MySQL 5.x
--    重跑幂等：相同 sort_order 下回填结果一致

-- 2.1) 初始化会话变量
SET @dict_color_rn := 0;
SET @dict_color_type := '';

-- 2.2) 用临时表计算每个 dict_type 内的行号（避免 MySQL 5.x 不允许在 UPDATE 中直接引用同表子查询的限制）
DROP TEMPORARY TABLE IF EXISTS dict_color_tmp;
CREATE TEMPORARY TABLE dict_color_tmp AS
SELECT id,
       @dict_color_rn := IF(@dict_color_type = dict_type, @dict_color_rn + 1, 1) AS rn,
       @dict_color_type := dict_type AS _t
FROM sys_dict
ORDER BY dict_type, sort_order, id;

-- 2.3) 关联临时表更新 color
UPDATE sys_dict d
JOIN dict_color_tmp r ON d.id = r.id
SET d.color = ELT(((r.rn - 1) % 24) + 1,
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#41b1e9',
    '#5b8ff9', '#5ad8a6', '#5d7092', '#f6bd16', '#e86452',
    '#6dc8ec', '#945fb9', '#ff9845', '#1e9493', '#ff99c3',
    '#3f4f5f', '#a1a0fc', '#2ec7c9', '#96dee8');

-- 2.4) 清理
DROP TEMPORARY TABLE dict_color_tmp;

-- 3) 校验
SELECT dict_type, COUNT(*) AS total, COUNT(color) AS with_color
FROM sys_dict
GROUP BY dict_type;
