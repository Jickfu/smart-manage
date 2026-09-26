INSERT INTO t_sys_app (id, name, number, icon, domain_id, seq) VALUES
(471000000000000001, '办公样板', 'office', 'CalendarOutlined', 430000000000000001, 20);
INSERT INTO t_sys_feature (id, feature_key, app_id, default_name, default_seq) VALUES
(471000000000000002, 'demo/office/leave', 471000000000000001, '请假申请', 10);
INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(471000000000000010, '请假申请-入口', 'demo:office:leave', 471000000000000002),
(471000000000000011, '请假申请-列表', 'demo:office:leave:listPage', 471000000000000002),
(471000000000000012, '请假申请-详情', 'demo:office:leave:detail', 471000000000000002),
(471000000000000013, '请假申请-保存', 'demo:office:leave:save', 471000000000000002),
(471000000000000014, '请假申请-提交', 'demo:office:leave:submit', 471000000000000002),
(471000000000000015, '请假申请-删除', 'demo:office:leave:delete', 471000000000000002);
INSERT INTO t_sys_menu (id, number, name, level, app_id, permission_id, path, component, icon, sort, feature_id, target_type) VALUES
(471000000000000020, 'leave', '请假申请', 1, 471000000000000001, 471000000000000010,
 '/demo/office/leave', 'demo/office/leave', 'CalendarOutlined', 10, 471000000000000002, 'INTERNAL_PAGE');
INSERT INTO t_sys_number_reference (id, reference_key, feature_id, name, system_preset) VALUES
(471000000000000030, 'demo/office/leave.number', 471000000000000002, '请假申请编号', true);
INSERT INTO t_sys_number_rule (id, rule_key, name, pattern, scope_type, reset_period, reference_key, system_preset) VALUES
(471000000000000031, 'demo/office/leave', '请假申请编号', 'LV-{bill.bizDate:yyyyMMdd}-{seq:5}', 'ORG', 'DAY', 'demo/office/leave.number', true);
INSERT INTO t_sys_number_rule_segment (id, rule_key, sort, segment_type, value, format, length, separator)
VALUES (471000000000000041, 'demo/office/leave', 1, 'FIXED', 'LV', NULL, NULL, '-'),
       (471000000000000042, 'demo/office/leave', 2, 'DATE', 'bill.bizDate', 'yyyyMMdd', NULL, '-'),
       (471000000000000043, 'demo/office/leave', 3, 'SEQUENCE', NULL, NULL, 5, '');
UPDATE t_sys_number_reference SET default_rule_key = 'demo/office/leave' WHERE reference_key = 'demo/office/leave.number';
