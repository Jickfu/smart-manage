-- 先创建编号引用，再建规则并回填默认规则，保持平台外键全程生效。
INSERT INTO public.t_sys_domain VALUES (430000000000000001, '演示', 'demo', 10, true, '2026-07-27 17:59:01.999094', '2026-09-02 12:35:22.927639', NULL, 1, 1);
INSERT INTO public.t_sys_app VALUES (430000000000000002, '采购管理', 'procurement', 'ShoppingCartOutlined', 1, '采购业务管理', 430000000000000001, true, '2026-07-27 17:59:01.999094', '2026-07-27 17:59:01.999094', '#1677ff', NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000002, 'demo/procurement/purchase-requisition', 430000000000000002, '采购申请', NULL, 20, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_number_reference VALUES (461000000000000001, 'demo/procurement/purchase-requisition.number', 450000000000000002, '采购申请编号', NULL, true, '采购申请业务编号', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_number_rule VALUES (460000000000000001, 'demo/procurement/purchase-requisition', '采购申请编号', 'PR-{bill.bizDate:yyyyMMdd}-{seq:5}', 'ORG', 'DAY', 1, true, true, '采购申请按组织、业务日期独立流水；格式可按需加入受控变量 org.number', NULL, NULL, NULL, NULL, 0, 'demo/procurement/purchase-requisition.number');
INSERT INTO public.t_sys_number_rule_segment VALUES (461000000000000011, 'demo/procurement/purchase-requisition', 1, 'FIXED', 'PR', NULL, NULL, '-');
INSERT INTO public.t_sys_number_rule_segment VALUES (461000000000000012, 'demo/procurement/purchase-requisition', 2, 'DATE', 'bill.bizDate', 'yyyyMMdd', NULL, '-');
INSERT INTO public.t_sys_number_rule_segment VALUES (461000000000000013, 'demo/procurement/purchase-requisition', 3, 'SEQUENCE', NULL, NULL, 5, '');
INSERT INTO public.t_sys_permission VALUES (510000000000000002, '采购申请-导出', 'demo:procurement:purchase-requisition:export', NULL, NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000010, '采购申请', 'demo:procurement:purchase-requisition', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000011, '采购申请-列表', 'demo:procurement:purchase-requisition:listPage', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000012, '采购申请-详情', 'demo:procurement:purchase-requisition:detail', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000013, '采购申请-保存', 'demo:procurement:purchase-requisition:save', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000014, '采购申请-提交', 'demo:procurement:purchase-requisition:submit', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000015, '采购申请-删除', 'demo:procurement:purchase-requisition:delete', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_menu VALUES (430000000000000020, 'purchase_requisition', '采购申请', 1, 0, 430000000000000002, 430000000000000010, '/demo/procurement/purchase-requisition', 'demo/procurement/purchase-requisition', 'FileAddOutlined', '采购申请单', 10, true, '2026-07-27 17:59:01.999094', '2026-08-19 16:35:38.652014', NULL, NULL, 3, 450000000000000002, 'INTERNAL_PAGE', NULL, NULL);
UPDATE public.t_sys_number_reference SET default_rule_key = 'demo/procurement/purchase-requisition' WHERE reference_key = 'demo/procurement/purchase-requisition.number';

-- 仅显式装配 Demo 时初始化演示部门；沿用平台公司，不覆盖已有组织或管理员凭据。
INSERT INTO public.t_sys_org VALUES (2087035058459361282, '领导层', '101', 1, 2, '2026-08-11 12:35:07.831649', '2026-08-12 17:12:51.329632', 1, NULL, 'SM/101', 'SM有限公司/领导层', 'DEPARTMENT', true, false, NULL, NULL, 1);
INSERT INTO public.t_sys_org VALUES (2087035439688040449, '财务部', '102', 1, 3, '2026-08-11 12:36:38.723212', '2026-08-12 17:12:51.329632', 1, NULL, 'SM/102', 'SM有限公司/财务部', 'DEPARTMENT', true, false, NULL, NULL, 1);
INSERT INTO public.t_sys_org VALUES (2096228918898434050, '销售部', '103', 1, 4, '2026-09-05 21:28:14.973521', '2026-09-05 21:28:14.968869', 1, NULL, 'SM/103', 'SM有限公司/销售部', 'DEPARTMENT', true, false, NULL, NULL, 0);

-- 请假样板目录与编号规则随未发布 Demo 基线一次初始化。
INSERT INTO public.t_sys_app (id, name, number, icon, domain_id, seq) VALUES
(471000000000000001, '办公样板', 'office', 'CalendarOutlined', 430000000000000001, 20);
INSERT INTO public.t_sys_feature (id, feature_key, app_id, default_name, default_seq) VALUES
(471000000000000002, 'demo/office/leave', 471000000000000001, '请假申请', 10);
INSERT INTO public.t_sys_permission (id, name, number, feature_id) VALUES
(471000000000000010, '请假申请-入口', 'demo:office:leave', 471000000000000002),
(471000000000000011, '请假申请-列表', 'demo:office:leave:listPage', 471000000000000002),
(471000000000000012, '请假申请-详情', 'demo:office:leave:detail', 471000000000000002),
(471000000000000013, '请假申请-保存', 'demo:office:leave:save', 471000000000000002),
(471000000000000014, '请假申请-提交', 'demo:office:leave:submit', 471000000000000002),
(471000000000000015, '请假申请-删除', 'demo:office:leave:delete', 471000000000000002);
INSERT INTO public.t_sys_menu (id, number, name, level, app_id, permission_id, path, component, icon, sort, feature_id, target_type) VALUES
(471000000000000020, 'leave', '请假申请', 1, 471000000000000001, 471000000000000010,
 '/demo/office/leave', 'demo/office/leave', 'CalendarOutlined', 10, 471000000000000002, 'INTERNAL_PAGE');
INSERT INTO public.t_sys_number_reference (id, reference_key, feature_id, name, system_preset) VALUES
(471000000000000030, 'demo/office/leave.number', 471000000000000002, '请假申请编号', true);
INSERT INTO public.t_sys_number_rule (id, rule_key, name, pattern, scope_type, reset_period, reference_key, system_preset) VALUES
(471000000000000031, 'demo/office/leave', '请假申请编号', 'LV-{bill.bizDate:yyyyMMdd}-{seq:5}', 'ORG', 'DAY', 'demo/office/leave.number', true);
INSERT INTO public.t_sys_number_rule_segment (id, rule_key, sort, segment_type, value, format, length, separator)
VALUES (471000000000000041, 'demo/office/leave', 1, 'FIXED', 'LV', NULL, NULL, '-'),
       (471000000000000042, 'demo/office/leave', 2, 'DATE', 'bill.bizDate', 'yyyyMMdd', NULL, '-'),
       (471000000000000043, 'demo/office/leave', 3, 'SEQUENCE', NULL, NULL, 5, '');
UPDATE public.t_sys_number_reference SET default_rule_key = 'demo/office/leave' WHERE reference_key = 'demo/office/leave.number';
