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
