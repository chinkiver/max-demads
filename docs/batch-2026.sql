-- 2026年投产批次初始化脚本
-- 蓝色 = 常规投产，绿色 = 标准投产
-- 执行前清空 batch 表

TRUNCATE TABLE batch;

INSERT INTO batch (batch_no, batch_type, batch_date, status) VALUES
-- 1月
('20260108-标准投产', 'standard_production', '2026-01-08', 'planning'),
('20260115-常规投产', 'routine_production', '2026-01-15', 'planning'),
('20260122-标准投产', 'standard_production', '2026-01-22', 'planning'),
('20260129-常规投产', 'routine_production', '2026-01-29', 'planning'),
-- 2月
('20260205-标准投产', 'standard_production', '2026-02-05', 'planning'),
('20260212-常规投产', 'routine_production', '2026-02-12', 'planning'),
('20260219-常规投产', 'routine_production', '2026-02-19', 'planning'),
('20260226-标准投产', 'standard_production', '2026-02-26', 'planning'),
-- 3月
('20260305-标准投产', 'standard_production', '2026-03-05', 'planning'),
('20260312-常规投产', 'routine_production', '2026-03-12', 'planning'),
('20260319-常规投产', 'routine_production', '2026-03-19', 'planning'),
('20260326-标准投产', 'standard_production', '2026-03-26', 'planning'),
-- 4月
('20260402-标准投产', 'standard_production', '2026-04-02', 'planning'),
('20260409-常规投产', 'routine_production', '2026-04-09', 'planning'),
('20260416-常规投产', 'routine_production', '2026-04-16', 'planning'),
('20260423-常规投产', 'routine_production', '2026-04-23', 'planning'),
('20260429-标准投产', 'standard_production', '2026-04-29', 'planning'),
-- 5月
('20260507-标准投产', 'standard_production', '2026-05-07', 'planning'),
('20260514-常规投产', 'routine_production', '2026-05-14', 'planning'),
('20260521-标准投产', 'standard_production', '2026-05-21', 'planning'),
('20260528-常规投产', 'routine_production', '2026-05-28', 'planning'),
-- 6月
('20260604-标准投产', 'standard_production', '2026-06-04', 'planning'),
('20260611-常规投产', 'routine_production', '2026-06-11', 'planning'),
('20260618-常规投产', 'routine_production', '2026-06-18', 'planning'),
('20260625-标准投产', 'standard_production', '2026-06-25', 'planning'),
-- 7月
('20260702-标准投产', 'standard_production', '2026-07-02', 'planning'),
('20260709-常规投产', 'routine_production', '2026-07-09', 'planning'),
('20260716-常规投产', 'routine_production', '2026-07-16', 'planning'),
('20260723-常规投产', 'routine_production', '2026-07-23', 'planning'),
('20260730-标准投产', 'standard_production', '2026-07-30', 'planning'),
-- 8月
('20260806-标准投产', 'standard_production', '2026-08-06', 'planning'),
('20260809-常规投产', 'routine_production', '2026-08-09', 'planning'),
('20260813-常规投产', 'routine_production', '2026-08-13', 'planning'),
('20260820-常规投产', 'routine_production', '2026-08-20', 'planning'),
('20260827-常规投产', 'routine_production', '2026-08-27', 'planning'),
-- 9月
('20260903-标准投产', 'standard_production', '2026-09-03', 'planning'),
('20260910-常规投产', 'routine_production', '2026-09-10', 'planning'),
('20260917-常规投产', 'routine_production', '2026-09-17', 'planning'),
('20260924-标准投产', 'standard_production', '2026-09-24', 'planning'),
-- 10月
('20261008-常规投产', 'routine_production', '2026-10-08', 'planning'),
('20261015-标准投产', 'standard_production', '2026-10-15', 'planning'),
('20261022-常规投产', 'routine_production', '2026-10-22', 'planning'),
('20261029-标准投产', 'standard_production', '2026-10-29', 'planning'),
-- 11月
('20261105-标准投产', 'standard_production', '2026-11-05', 'planning'),
('20261112-常规投产', 'routine_production', '2026-11-12', 'planning'),
('20261119-常规投产', 'routine_production', '2026-11-19', 'planning'),
('20261126-标准投产', 'standard_production', '2026-11-26', 'planning'),
-- 12月
('20261203-标准投产', 'standard_production', '2026-12-03', 'planning'),
('20261210-常规投产', 'routine_production', '2026-12-10', 'planning'),
('20261217-常规投产', 'routine_production', '2026-12-17', 'planning'),
('20261224-标准投产', 'standard_production', '2026-12-24', 'planning');
