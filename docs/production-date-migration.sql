-- 为已投产需求增加实际投产日期字段
ALTER TABLE biz_requirement ADD COLUMN production_date DATE COMMENT '实际投产日期' AFTER batch_id;
