--
-- PostgreSQL database dump
--


-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: t_sys_domain; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_domain VALUES (4, '系统服务', 'sys', 99, true, '2026-04-22 13:47:23.710312', '2026-06-11 23:20:53.345438', NULL, 1, 0);
INSERT INTO public.t_sys_domain VALUES (430000000000000001, '供应链', 'scm', 10, true, '2026-07-27 17:59:01.999094', '2026-09-02 12:35:22.927639', NULL, 1, 1);


--
-- Data for Name: t_sys_app; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_app VALUES (430000000000000002, '采购管理', 'procurement', 'ShoppingCartOutlined', 1, '采购业务管理', 430000000000000001, true, '2026-07-27 17:59:01.999094', '2026-07-27 17:59:01.999094', '#1677ff', NULL, NULL, 0);
INSERT INTO public.t_sys_app VALUES (32, '任务调度', 'scheduler', 'ClockCircleOutlined', 3, '定时任务定义与执行实例管理', 4, true, '2026-07-30 23:40:19.392006', '2026-07-30 23:40:19.392006', '#1677ff', NULL, NULL, 0);
INSERT INTO public.t_sys_app VALUES (31, '系统管理', 'base', 'AppstoreOutlined', 1, '组织、权限、资料、平台结构与系统配置', 4, true, '2026-04-22 18:06:56.092765', '2026-08-19 16:16:00.874373', '#1BA854', NULL, 1, 2);
INSERT INTO public.t_sys_app VALUES (30, '运维中心', 'monitor', 'DashboardOutlined', 2, '运行监控、审计日志、诊断与高风险运维工具', 4, true, '2026-04-22 13:47:23.710312', '2026-08-19 16:16:00.874373', '#fa8c16', NULL, 1, 2);
INSERT INTO public.t_sys_app VALUES (470000000000001000, '消息服务', 'message', 'MailOutlined', 4, '站内消息、发信账号、管理员邮件投递与发送记录', 4, true, '2026-08-22 13:42:50.92663', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 1);


--
-- Data for Name: t_sys_attachment_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_attachment_config VALUES (420000000000001101, 20971520, 'pdf,png,jpg,jpeg,gif,webp,doc,docx,xls,xlsx,ppt,pptx,txt', 'application/pdf,image/png,image/jpeg,image/gif,image/webp,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,text/plain', 24, 0, '2026-08-06 23:55:26.160595', NULL, NULL, NULL);


--
-- Data for Name: t_sys_number_rule; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_number_rule VALUES (460000000000000001, 'scm/procurement/purchase-requisition', '采购申请编号', 'PR-{bill.bizDate:yyyyMMdd}-{seq:5}', 'ORG', 'DAY', 1, true, true, '采购申请按组织、业务日期独立流水；格式可按需加入受控变量 org.number', NULL, NULL, NULL, NULL, 0, 'scm/procurement/purchase-requisition.number');
INSERT INTO public.t_sys_number_rule VALUES (460000000000000002, 'sys/base/basic-data-item', '基础资料编号', 'BD-{seq:4}', 'CATEGORY', 'NEVER', 1, true, true, '基础资料按分类独立流水', NULL, '2026-09-02 12:03:23.920844', NULL, 1, 1, 'sys/base/basic-data-item.number');


--
-- Data for Name: t_sys_basic_data_category; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_basic_data_item; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: public.t_sys_feature; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_feature VALUES (450000000000000002, 'scm/procurement/purchase-requisition', 430000000000000002, '采购申请', NULL, 20, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000004, 'sys/base/app', 31, '应用管理', NULL, 20, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000005, 'sys/base/attachment-config', 31, '附件配置', NULL, 30, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000009, 'sys/base/menu', 31, '菜单管理', NULL, 70, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000010, 'sys/base/org', 31, '组织管理', NULL, 80, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000013, 'sys/base/role', 31, '角色管理', NULL, 110, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000015, 'sys/base/user', 31, '用户管理', NULL, 130, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000017, 'sys/log/login', 30, '登录日志', NULL, 20, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000018, 'sys/log/operate', 30, '操作日志', NULL, 30, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000019, 'sys/monitor', 30, '脚本控制台', NULL, 40, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000022, 'sys/monitor/script', 30, '脚本控制台', NULL, 70, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000023, 'sys/monitor/script/log', 30, '脚本执行历史', NULL, 80, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000025, 'sys/monitor/sql', 30, 'SQL控制台', NULL, 100, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000026, 'sys/monitor/sql/log', 30, 'SQL执行历史', NULL, 110, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000027, 'sys/monitor/thread', 30, '线程诊断', NULL, 120, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000100, 'sys/base/feature', 31, '功能管理', NULL, 40, NULL, '维护系统功能的展示名称、排序、描述和目录可见性', true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (460000000000000010, 'sys/base/number-rule', 31, '编号规则', NULL, 45, NULL, '维护业务单据和主数据的编号模板、作用域与重置周期', true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000006, 'sys/base/basic-data', 31, '基础资料', NULL, 40, NULL, NULL, true, 'SYSTEM', NULL, '2026-08-19 16:16:00.874373', NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (450000000000000008, 'sys/base/file-config', 31, '存储配置', NULL, 60, NULL, NULL, true, 'SYSTEM', NULL, '2026-08-19 16:16:00.874373', NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (450000000000000012, 'sys/base/permission', 31, '权限定义', NULL, 100, NULL, NULL, true, 'SYSTEM', NULL, '2026-08-19 16:16:00.874373', NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (450000000000000029, 'sys/scheduler/execution', 32, '执行记录', NULL, 20, NULL, NULL, true, 'SYSTEM', NULL, '2026-08-19 16:16:00.874373', NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (450000000000000007, 'sys/base/domain', 31, '领域管理', NULL, 50, NULL, NULL, true, 'SYSTEM', NULL, '2026-08-20 16:06:19.882674', NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (480000000000000100, 'sys/base/login-protection', 31, '登录保护', NULL, 65, NULL, '登录验证码、失败限制和短时保护等认证安全能力', true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000011, 'sys/base/param', 31, '系统参数', NULL, 90, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (470000000000001010, 'sys/message/email-account', 470000000000001000, '发信账号', NULL, 10, NULL, '维护 SMTP 发信账号及全局默认账号', true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (470000000000001020, 'sys/message/email-compose', 470000000000001000, '发送邮件', NULL, 20, NULL, '由超级管理员创建正式邮件投递任务', true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (470000000000001030, 'sys/message/email-record', 470000000000001000, '发送记录', NULL, 30, NULL, '查询、取消和重新发送邮件投递任务', true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000021, 'sys/monitor/runtime', 30, '运行监控', NULL, 60, NULL, '主机、应用实例、实时快照与历史趋势', true, 'SYSTEM', NULL, '2026-08-25 14:45:23.512402', NULL, NULL, 2);
INSERT INTO public.t_sys_feature VALUES (450000000000000041, 'sys/monitor/alert', 30, '监控告警', NULL, 65, NULL, '配置预定义告警规则并查询告警事件', true, 'SYSTEM', '2026-08-25 14:45:23.512402', '2026-08-25 14:45:23.512402', NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (510000000000000010, 'sys/message/inbox-broadcast', 470000000000001000, '消息发布', NULL, 10, NULL, '创建、发布和查询全站站内消息', true, 'SYSTEM', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (520000000000000010, 'sys/base/openapi-application', 31, '第三方应用', NULL, 10, NULL, '第三方调用方、凭据和 API 授权', true, 'SYSTEM', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (520000000000000030, 'sys/base/openapi-invocation', 31, '调用监控', NULL, 30, NULL, 'OpenAPI 调用日志与统计', true, 'SYSTEM', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000014, 'sys/base/ui-config', 31, '界面配置', NULL, 120, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000020, 'sys/monitor/cache', 30, '缓存管理', NULL, 50, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000030, 'sys/scheduler/job', 32, '定时任务', NULL, 30, NULL, NULL, true, 'SYSTEM', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_feature VALUES (450000000000000024, 'sys/monitor/slow-sql', 30, '慢SQL分析', NULL, 90, NULL, NULL, true, 'SYSTEM', NULL, '2026-08-19 16:16:00.874373', NULL, NULL, 1);
INSERT INTO public.t_sys_feature VALUES (520000000000000020, 'sys/base/openapi-catalog', 31, 'API文档', NULL, 20, NULL, '显式注册的 API 版本、协议和文档', true, 'SYSTEM', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0);


--
-- Data for Name: t_sys_file_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_file_config VALUES (2082478248768778241, 'LOCAL', './smfiles/', NULL, 21, NULL, NULL, true, '2026-07-29 22:47:59.740268', '2026-09-01 10:57:48.107604', 1, 1, NULL, 4, NULL, NULL, NULL, NULL, NULL, true);


--
-- Data for Name: t_sys_job; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_job VALUES (470000000000001301, '邮件投递派发', '处理持久化邮件任务并执行有限重试', 'sm.domain.sys.scheduler.job.DispatchEmailJob', '0/15 * * * * ?', '{"batchSize":20}', 'ENABLED', NULL, NULL, NULL, NULL, 'SYSTEM_EMAIL_DISPATCH', true, 0, 'system-email-dispatch', 470000000000001000);
INSERT INTO public.t_sys_job VALUES (2082857218823630850, '附件对象清理', '清理过期临时附件，并重试数据库已标记待删除的对象', 'sm.domain.sys.scheduler.job.CleanTempFileJob', '0 0/30 * * * ?', '{}', 'ENABLED', '2026-07-30 23:53:53.241535', '2026-08-26 21:53:56.414223', 1, NULL, 'ATTACHMENT_OBJECT_CLEANUP', true, 8, 'attachment-object-cleanup', 31);
INSERT INTO public.t_sys_job VALUES (510000000000000301, '站内消息发布派发', '为待发布消息生成当前启用用户收件快照', 'sm.domain.sys.scheduler.job.DispatchInboxMessageJob', '0/10 * * * * ?', '{"batchSize":5}', 'ENABLED', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 'SYSTEM_INBOX_MESSAGE_DISPATCH', true, 0, 'system-inbox-message-dispatch', 470000000000001000);
INSERT INTO public.t_sys_job VALUES (440000000000000001, '系统日志分区转储', '将超过在线保留期的完整月分区转入历史父表', 'sm.domain.sys.scheduler.job.ArchiveSystemLogJob', '0 10 2 * * ?', '{"jobLogHotDays": 90, "sqlLogHotDays": 180, "loginLogHotDays": 180, "scriptLogHotDays": 180, "openApiLogHotDays": 180, "operateLogHotDays": 180, "maxPartitionsPerRun": 12}', 'PAUSED', '2026-08-10 16:19:04.823455', '2026-08-31 21:18:52.196357', NULL, NULL, 'SYSTEM_LOG_ARCHIVE', true, 1, 'system-log-lifecycle', 30);
INSERT INTO public.t_sys_job VALUES (440000000000000002, '系统日志历史淘汰', '删除超过历史保留期的完整月分区', 'sm.domain.sys.scheduler.job.PurgeSystemLogHistoryJob', '0 40 2 * * ?', '{"jobLogRetentionDays": 365, "maxPartitionsPerRun": 12, "sqlLogRetentionDays": 730, "loginLogRetentionDays": 1095, "scriptLogRetentionDays": 730, "openApiLogRetentionDays": 730, "operateLogRetentionDays": 1095}', 'PAUSED', '2026-08-10 16:19:04.823455', '2026-08-31 21:18:52.196357', NULL, NULL, 'SYSTEM_LOG_HISTORY_PURGE', true, 1, 'system-log-lifecycle', 30);


--
-- Data for Name: public.t_sys_permission; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_permission VALUES (420000000000001024, '用户管理-生成代登录凭证', 'sys:base:user:temporaryLogin', '2026-08-12 21:06:36.597281', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001025, '用户管理-查看敏感信息', 'sys:base:user:sensitive:read', '2026-08-14 12:02:29.233875', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (460000000000000011, '编号规则-查询', 'sys:base:number-rule:listPage', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (460000000000000012, '编号规则-详情', 'sys:base:number-rule:detail', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (460000000000000013, '编号规则-保存', 'sys:base:number-rule:save', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (460000000000000014, '编号规则-删除', 'sys:base:number-rule:delete', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (460000000000000015, '编号规则-选择', 'sys:base:number-rule:select', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (460000000000000016, '编号规则-预览', 'sys:base:number-rule:preview', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (461000000000000031, '编号规则-启用', 'sys:base:number-rule:enable', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (461000000000000032, '编号规则-停用', 'sys:base:number-rule:disable', NULL, NULL, NULL, NULL, 0, 460000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (10031, '领域管理-查询', 'sys:base:domain:listPage', NULL, '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (10032, '领域管理-详情', 'sys:base:domain:detail', NULL, '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (10033, '领域管理-保存', 'sys:base:domain:save', NULL, '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (10034, '领域管理-删除', 'sys:base:domain:delete', NULL, '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001004, '领域管理-禁用', 'sys:base:domain:disable', '2026-07-27 17:59:01.958211', '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001017, '领域管理-选择', 'sys:base:domain:select', '2026-08-06 22:44:05.351384', '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001101, '发信账号-查询', 'sys:message:email-account:listPage', NULL, NULL, NULL, NULL, 0, 470000000000001010, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001102, '发信账号-详情', 'sys:message:email-account:detail', NULL, NULL, NULL, NULL, 0, 470000000000001010, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001103, '发信账号-保存', 'sys:message:email-account:save', NULL, NULL, NULL, NULL, 0, 470000000000001010, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001104, '发信账号-启停', 'sys:message:email-account:enable', NULL, NULL, NULL, NULL, 0, 470000000000001010, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001105, '发信账号-删除', 'sys:message:email-account:delete', NULL, NULL, NULL, NULL, 0, 470000000000001010, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001106, '发信账号-测试', 'sys:message:email-account:test', NULL, NULL, NULL, NULL, 0, 470000000000001010, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001111, '发送邮件-发送', 'sys:message:email-compose:send', NULL, NULL, NULL, NULL, 0, 470000000000001020, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001121, '发送记录-查询', 'sys:message:email-record:listPage', NULL, NULL, NULL, NULL, 0, 470000000000001030, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001122, '发送记录-详情', 'sys:message:email-record:detail', NULL, NULL, NULL, NULL, 0, 470000000000001030, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001123, '发送记录-重发', 'sys:message:email-record:retry', NULL, NULL, NULL, NULL, 0, 470000000000001030, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001124, '发送记录-取消', 'sys:message:email-record:cancel', NULL, NULL, NULL, NULL, 0, 470000000000001030, NULL);
INSERT INTO public.t_sys_permission VALUES (470000000000001100, '消息服务-访问', 'sys:message:access', NULL, NULL, NULL, NULL, 0, NULL, 470000000000001000);
INSERT INTO public.t_sys_permission VALUES (470000000000000101, '角色管理-分配数据范围', 'sys:base:role:assignDataScopes', NULL, NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (490000000000000103, '运行监控-管理', 'sys:monitor:runtime:manage', '2026-08-26 13:33:41.693108', '2026-08-26 13:33:41.693108', NULL, NULL, 0, 450000000000000021, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000101, '消息发布-查询', 'sys:message:inbox-broadcast:listPage', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 0, 510000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000102, '消息发布-详情', 'sys:message:inbox-broadcast:detail', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 0, 510000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000103, '消息发布-保存', 'sys:message:inbox-broadcast:save', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 0, 510000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000104, '消息发布-发布', 'sys:message:inbox-broadcast:publish', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 0, 510000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000105, '消息发布-重试', 'sys:message:inbox-broadcast:retry', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 0, 510000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000101, '第三方应用-查询', 'sys:base:openapi-application:listPage', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000102, '第三方应用-详情', 'sys:base:openapi-application:detail', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000103, '第三方应用-保存', 'sys:base:openapi-application:save', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000104, '第三方应用-启停', 'sys:base:openapi-application:enable', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000105, '第三方应用-凭据管理', 'sys:base:openapi-application:credential', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000106, '第三方应用-API授权', 'sys:base:openapi-application:grant', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000201, 'API文档-查询', 'sys:base:openapi-catalog:listPage', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000202, 'API版本-发布管理', 'sys:base:openapi-catalog:publish', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000301, '调用监控-查询', 'sys:base:openapi-invocation:listPage', '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 520000000000000030, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000001, '用户管理-导入', 'sys:base:user:import', NULL, NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000002, '采购申请-导出', 'scm:procurement:purchase-requisition:export', NULL, NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (520000000000000203, 'API文档-业务试调', 'sys:base:openapi-catalog:test', '2026-09-06 00:04:47.727664', NULL, NULL, NULL, 0, 520000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (10035, '应用管理-查询', 'sys:base:app:listPage', NULL, NULL, NULL, NULL, 0, 450000000000000004, NULL);
INSERT INTO public.t_sys_permission VALUES (10036, '应用管理-详情', 'sys:base:app:detail', NULL, NULL, NULL, NULL, 0, 450000000000000004, NULL);
INSERT INTO public.t_sys_permission VALUES (10037, '应用管理-保存', 'sys:base:app:save', NULL, NULL, NULL, NULL, 0, 450000000000000004, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000010, '采购申请', 'scm:procurement:purchase-requisition', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (10030, '系统管理-应用入口', 'sys:base:access', NULL, '2026-08-19 16:16:00.874373', NULL, 1, 1, NULL, 31);
INSERT INTO public.t_sys_permission VALUES (10020, '运维中心-应用入口', 'sys:log:access', NULL, '2026-08-19 16:16:00.874373', NULL, 1, 1, NULL, 30);
INSERT INTO public.t_sys_permission VALUES (490000000000000101, '监控告警-查看', 'sys:monitor:alert:view', '2026-08-25 14:45:23.512402', '2026-08-25 14:45:23.512402', NULL, NULL, 0, 450000000000000041, NULL);
INSERT INTO public.t_sys_permission VALUES (490000000000000102, '监控告警-管理', 'sys:monitor:alert:manage', '2026-08-25 14:45:23.512402', '2026-08-25 14:45:23.512402', NULL, NULL, 0, 450000000000000041, NULL);
INSERT INTO public.t_sys_permission VALUES (510000000000000003, '用户管理-导出', 'sys:base:user:export', NULL, NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (10038, '应用管理-删除', 'sys:base:app:delete', NULL, NULL, NULL, NULL, 0, 450000000000000004, NULL);
INSERT INTO public.t_sys_permission VALUES (10014, '权限管理-查询', 'sys:base:permission:listPage', NULL, NULL, NULL, NULL, 0, 450000000000000012, NULL);
INSERT INTO public.t_sys_permission VALUES (10012, '用户管理-查询', 'sys:base:user:listPage', NULL, NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (10015, '角色管理-查询', 'sys:base:role:listPage', NULL, NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (10016, '权限管理-保存', 'sys:base:permission:save', NULL, NULL, NULL, NULL, 0, 450000000000000012, NULL);
INSERT INTO public.t_sys_permission VALUES (10017, '权限管理-详情', 'sys:base:permission:detail', NULL, NULL, NULL, NULL, 0, 450000000000000012, NULL);
INSERT INTO public.t_sys_permission VALUES (10013, '菜单管理-查询', 'sys:base:menu:listPage', NULL, '2026-04-27 12:17:57.584541', NULL, 1, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (10039, '角色管理-详情', 'sys:base:role:detail', NULL, NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (10040, '角色管理-保存', 'sys:base:role:save', NULL, NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (10041, '权限管理-选择', 'sys:base:permission:select', '2026-04-27 13:45:08.495725', NULL, 1, NULL, 0, 450000000000000012, NULL);
INSERT INTO public.t_sys_permission VALUES (10042, '权限管理-删除', 'sys:base:permission:delete', '2026-04-27 13:45:51.39581', NULL, 1, NULL, 0, 450000000000000012, NULL);
INSERT INTO public.t_sys_permission VALUES (406250201727746048, '菜单管理-详情', 'sys:base:menu:detail', '2026-04-27 13:54:15.855314', NULL, 1, NULL, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (406254838245605376, '菜单管理-保存', 'sys:base:menu:save', '2026-04-27 14:12:41.287791', NULL, 1, NULL, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (406259661691011072, '菜单管理-选择', 'sys:base:menu:select', '2026-04-27 14:31:51.286645', NULL, 1, NULL, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (10022, '登录日志-查询', 'sys:log:login:listPage', NULL, '2026-04-27 15:38:27.280076', NULL, 1, 0, 450000000000000017, NULL);
INSERT INTO public.t_sys_permission VALUES (10023, '操作日志-查询', 'sys:log:operate:listPage', NULL, '2026-04-27 15:38:38.913184', NULL, 1, 0, 450000000000000018, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001010, '基础数据管理-禁用', 'sys:base:basic-data:disable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000006, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001011, '角色管理-分配权限', 'sys:base:role:assignPermissions', '2026-07-27 17:59:01.975551', NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001012, '用户管理-分配角色', 'sys:base:user:assignRoles', '2026-07-27 17:59:01.975551', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (413172783453237248, '界面配置列表', 'sys:base:ui-config:listPage', '2026-05-16 16:22:07.956473', NULL, NULL, NULL, 0, 450000000000000014, NULL);
INSERT INTO public.t_sys_permission VALUES (413172783499374592, '界面配置详情', 'sys:base:ui-config:detail', '2026-05-16 16:22:07.965499', NULL, NULL, NULL, 0, 450000000000000014, NULL);
INSERT INTO public.t_sys_permission VALUES (413172783507763200, '界面配置保存', 'sys:base:ui-config:save', '2026-05-16 16:22:07.968478', NULL, NULL, NULL, 0, 450000000000000014, NULL);
INSERT INTO public.t_sys_permission VALUES (413196675722964992, '文件配置列表', 'sys:base:file-config:listPage', '2026-05-16 17:57:04.317767', NULL, NULL, NULL, 0, 450000000000000008, NULL);
INSERT INTO public.t_sys_permission VALUES (413196675756519424, '文件配置详情', 'sys:base:file-config:detail', '2026-05-16 17:57:04.323767', NULL, NULL, NULL, 0, 450000000000000008, NULL);
INSERT INTO public.t_sys_permission VALUES (413196675764908032, '文件配置保存', 'sys:base:file-config:save', '2026-05-16 17:57:04.325767', NULL, NULL, NULL, 0, 450000000000000008, NULL);
INSERT INTO public.t_sys_permission VALUES (50050, '系统参数分类', 'sys:base:param:category', '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0, 450000000000000011, NULL);
INSERT INTO public.t_sys_permission VALUES (50051, '系统参数列表', 'sys:base:param:listPage', '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0, 450000000000000011, NULL);
INSERT INTO public.t_sys_permission VALUES (50052, '系统参数详情', 'sys:base:param:detail', '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0, 450000000000000011, NULL);
INSERT INTO public.t_sys_permission VALUES (50053, '系统参数编辑', 'sys:base:param:save', '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0, 450000000000000011, NULL);
INSERT INTO public.t_sys_permission VALUES (50054, '系统参数删除', 'sys:base:param:delete', '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0, 450000000000000011, NULL);
INSERT INTO public.t_sys_permission VALUES (50070, '缓存管理列表', 'sys:monitor:cache:listPage', '2026-05-17 20:09:21.311371', NULL, NULL, NULL, 0, 450000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (411644663060602880, '基础数据管理-删除', 'sys:base:basic-data:delete', '2026-05-12 11:09:55.661809', '2026-05-12 11:15:10.76583', NULL, 1, 0, 450000000000000006, NULL);
INSERT INTO public.t_sys_permission VALUES (411644663027048448, '基础数据管理-详情', 'sys:base:basic-data:detail', '2026-05-12 11:09:55.653679', '2026-05-12 11:15:21.394506', NULL, 1, 0, 450000000000000006, NULL);
INSERT INTO public.t_sys_permission VALUES (411644662943162368, '基础数据管理-列表', 'sys:base:basic-data:listPage', '2026-05-12 11:09:55.638553', '2026-05-12 11:15:30.967268', NULL, 1, 0, 450000000000000006, NULL);
INSERT INTO public.t_sys_permission VALUES (411644663043825664, '基础数据管理-保存', 'sys:base:basic-data:save', '2026-05-12 11:09:55.65768', '2026-05-12 11:15:44.892574', NULL, 1, 0, 450000000000000006, NULL);
INSERT INTO public.t_sys_permission VALUES (419000000000000006, '脚本控制台', 'sys:monitor:script', NULL, NULL, NULL, NULL, 0, 450000000000000019, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001001, '用户管理-启用', 'sys:base:user:enable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001002, '用户管理-禁用', 'sys:base:user:disable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001005, '应用管理-启用', 'sys:base:app:enable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000004, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001006, '应用管理-禁用', 'sys:base:app:disable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000004, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001007, '菜单管理-启用', 'sys:base:menu:enable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001008, '菜单管理-禁用', 'sys:base:menu:disable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001009, '基础数据管理-启用', 'sys:base:basic-data:enable', '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0, 450000000000000006, NULL);
INSERT INTO public.t_sys_permission VALUES (50071, '应用缓存-清理', 'sys:monitor:cache:clear', '2026-05-17 20:09:21.311371', '2026-08-03 21:22:35.436359', NULL, NULL, 1, 450000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (413501707400000001, 'SQL控制台-执行', 'sys:monitor:sql:execute', '2026-05-18 00:27:43.408601', '2026-08-04 22:10:07.527334', NULL, NULL, 1, 450000000000000025, NULL);
INSERT INTO public.t_sys_permission VALUES (413501707400000003, 'SQL执行历史-列表', 'sys:monitor:sql:log:listPage', '2026-05-18 00:43:01.026314', '2026-08-04 22:10:07.527334', NULL, NULL, 1, 450000000000000026, NULL);
INSERT INTO public.t_sys_permission VALUES (419000000000000001, '脚本控制台-执行', 'sys:monitor:script:execute', NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1, 450000000000000022, NULL);
INSERT INTO public.t_sys_permission VALUES (419000000000000002, '脚本管理-列表', 'sys:monitor:script:listPage', NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1, 450000000000000022, NULL);
INSERT INTO public.t_sys_permission VALUES (419000000000000003, '脚本管理-详情', 'sys:monitor:script:detail', NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1, 450000000000000022, NULL);
INSERT INTO public.t_sys_permission VALUES (419000000000000004, '脚本管理-保存', 'sys:monitor:script:save', NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1, 450000000000000022, NULL);
INSERT INTO public.t_sys_permission VALUES (419000000000000005, '脚本管理-删除', 'sys:monitor:script:delete', NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1, 450000000000000022, NULL);
INSERT INTO public.t_sys_permission VALUES (413501707320168448, '线程诊断-采集', 'sys:monitor:thread:collect', '2026-05-17 14:09:09.515591', '2026-08-10 12:34:28.537208', NULL, NULL, 2, 450000000000000027, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000011, '采购申请-列表', 'scm:procurement:purchase-requisition:listPage', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000012, '采购申请-详情', 'scm:procurement:purchase-requisition:detail', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000013, '采购申请-保存', 'scm:procurement:purchase-requisition:save', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000014, '采购申请-提交', 'scm:procurement:purchase-requisition:submit', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (430000000000000015, '采购申请-删除', 'scm:procurement:purchase-requisition:delete', '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0, 450000000000000002, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001013, '用户管理-重置密码', 'sys:base:user:resetPassword', '2026-07-29 17:34:17.647656', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001014, '登录日志-详情', 'sys:log:login:detail', '2026-07-30 18:45:20.440561', NULL, NULL, NULL, 0, 450000000000000017, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001015, '操作日志-详情', 'sys:log:operate:detail', '2026-07-30 18:45:20.440561', NULL, NULL, NULL, 0, 450000000000000018, NULL);
INSERT INTO public.t_sys_permission VALUES (413260828487667712, '定时任务列表', 'sys:scheduler:job:listPage', '2026-05-16 22:11:59.528179', '2026-07-30 23:40:19.392006', NULL, NULL, 1, 450000000000000030, NULL);
INSERT INTO public.t_sys_permission VALUES (413260828529610752, '定时任务详情', 'sys:scheduler:job:detail', '2026-05-16 22:11:59.536189', '2026-07-30 23:40:19.392006', NULL, NULL, 1, 450000000000000030, NULL);
INSERT INTO public.t_sys_permission VALUES (413260828537999360, '定时任务编辑', 'sys:scheduler:job:save', '2026-05-16 22:11:59.538188', '2026-07-30 23:40:19.392006', NULL, NULL, 1, 450000000000000030, NULL);
INSERT INTO public.t_sys_permission VALUES (413260828546387968, '定时任务删除', 'sys:scheduler:job:delete', '2026-05-16 22:11:59.540189', '2026-07-30 23:40:19.392006', NULL, NULL, 1, 450000000000000030, NULL);
INSERT INTO public.t_sys_permission VALUES (413260828550582272, '执行实例列表', 'sys:scheduler:execution:listPage', '2026-05-16 22:11:59.541187', '2026-07-30 23:40:19.392006', NULL, NULL, 1, 450000000000000029, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001016, '执行实例-详情', 'sys:scheduler:execution:detail', '2026-07-30 23:40:19.392006', NULL, NULL, NULL, 0, 450000000000000029, NULL);
INSERT INTO public.t_sys_permission VALUES (421000000000000001, '应用缓存-全部清理', 'sys:monitor:cache:clearAll', '2026-08-03 21:22:35.436359', NULL, NULL, NULL, 0, 450000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (421000000000000005, '缓存管理-查看值', 'sys:monitor:cache:value', '2026-08-03 21:59:34.573686', NULL, NULL, NULL, 0, 450000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (421000000000000006, '缓存管理-删除', 'sys:monitor:cache:delete', '2026-08-03 21:59:34.573686', NULL, NULL, NULL, 0, 450000000000000020, NULL);
INSERT INTO public.t_sys_permission VALUES (425000000000000001, 'SQL执行历史-详情', 'sys:monitor:sql:log:detail', '2026-08-04 22:10:07.527334', NULL, NULL, NULL, 0, 450000000000000026, NULL);
INSERT INTO public.t_sys_permission VALUES (426000000000000001, '脚本执行历史-列表', 'sys:monitor:script:log:listPage', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, 0, 450000000000000023, NULL);
INSERT INTO public.t_sys_permission VALUES (426000000000000002, '脚本执行历史-详情', 'sys:monitor:script:log:detail', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, 0, 450000000000000023, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001018, '菜单管理-删除', 'sys:base:menu:delete', '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0, 450000000000000009, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001019, '角色管理-选择', 'sys:base:role:select', '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001020, '角色管理-删除', 'sys:base:role:delete', '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0, 450000000000000013, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001021, '用户管理-详情', 'sys:base:user:detail', '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001022, '用户管理-保存', 'sys:base:user:save', '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001003, '领域管理-启用', 'sys:base:domain:enable', '2026-07-27 17:59:01.958211', '2026-08-20 16:06:19.884771', NULL, NULL, 1, 450000000000000007, NULL);
INSERT INTO public.t_sys_permission VALUES (413501707269836800, '运行监控-查看', 'sys:monitor:runtime:view', '2026-05-17 14:09:09.506432', '2026-08-25 14:45:23.512402', NULL, NULL, 2, 450000000000000021, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001023, '用户管理-删除', 'sys:base:user:delete', '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0, 450000000000000015, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001102, '附件配置-查看', 'sys:base:attachment-config:detail', '2026-08-06 23:55:26.160595', NULL, NULL, NULL, 0, 450000000000000005, NULL);
INSERT INTO public.t_sys_permission VALUES (420000000000001103, '附件配置-保存', 'sys:base:attachment-config:save', '2026-08-06 23:55:26.160595', NULL, NULL, NULL, 0, 450000000000000005, NULL);
INSERT INTO public.t_sys_permission VALUES (438000000000000001, '线程诊断-访问', 'sys:monitor:thread:access', '2026-08-10 10:36:56.930081', '2026-08-10 12:34:28.537208', NULL, NULL, 1, 450000000000000027, NULL);
INSERT INTO public.t_sys_permission VALUES (441000000000000001, '慢SQL监控-访问', 'sys:monitor:slow-sql:access', '2026-08-10 17:22:51.566885', NULL, NULL, NULL, 0, 450000000000000024, NULL);
INSERT INTO public.t_sys_permission VALUES (441000000000000002, '慢SQL监控-配置', 'sys:monitor:slow-sql:config', '2026-08-10 17:22:51.566885', NULL, NULL, NULL, 0, 450000000000000024, NULL);
INSERT INTO public.t_sys_permission VALUES (441000000000000003, '慢SQL监控-清空', 'sys:monitor:slow-sql:clear', '2026-08-10 17:22:51.566885', NULL, NULL, NULL, 0, 450000000000000024, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000001, '组织管理-查询', 'sys:base:org:listPage', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000002, '组织管理-详情', 'sys:base:org:detail', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000003, '组织管理-保存', 'sys:base:org:save', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000004, '组织管理-启用', 'sys:base:org:enable', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000005, '组织管理-禁用', 'sys:base:org:disable', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000006, '组织管理-封存', 'sys:base:org:archive', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000007, '组织管理-解封', 'sys:base:org:unarchive', NULL, NULL, NULL, NULL, 0, 450000000000000010, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000101, '功能管理-查询', 'sys:base:feature:listPage', NULL, NULL, NULL, NULL, 0, 450000000000000100, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000102, '功能管理-详情', 'sys:base:feature:detail', NULL, NULL, NULL, NULL, 0, 450000000000000100, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000103, '功能管理-保存', 'sys:base:feature:save', NULL, NULL, NULL, NULL, 0, 450000000000000100, NULL);
INSERT INTO public.t_sys_permission VALUES (450000000000000104, '功能管理-选择', 'sys:base:feature:select', NULL, NULL, NULL, NULL, 0, 450000000000000100, NULL);


--
-- Data for Name: public.t_sys_menu; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_menu VALUES (510000000000000200, 'inbox_message', '站内消息', 0, 0, 470000000000001000, 470000000000001100, NULL, NULL, 'NotificationOutlined', '全站消息发布与用户消息中心', 5, true, '2026-08-28 17:59:18.631332', '2026-08-28 17:59:18.631332', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (510000000000000201, 'inbox_broadcast', '消息发布', 1, 510000000000000200, 470000000000001000, 510000000000000101, '/sys/message/inbox-broadcast', 'sys/message/inbox-broadcast', 'NotificationOutlined', '创建并发布全站站内消息', 10, true, '2026-08-28 17:59:18.631332', '2026-08-28 17:59:18.631332', NULL, NULL, 0, 510000000000000010, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413260828563165184, 'job', '定时任务', 1, 0, 32, 413260828487667712, '/sys/scheduler/job', 'sys/scheduler/job', 'ClockCircleOutlined', NULL, 10, true, '2026-05-16 22:11:59.544189', '2026-08-19 16:35:38.652014', NULL, NULL, 3, 450000000000000030, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (450000000000000010, 'org', '组织管理', 1, 470000000000000001, 31, 450000000000000001, '/sys/base/org', 'sys/base/org', 'ApartmentOutlined', '行政组织管理', 10, true, '2026-08-11 12:21:26.724557', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000010, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (2103, 'menu', '菜单管理', 1, 470000000000000004, 31, 10013, '/sys/base/menu', 'sys/base/menu', 'MenuOutlined', '菜单', 40, true, '2026-04-14 13:59:27.544725', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000009, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (2105, 'role', '角色管理', 1, 470000000000000002, 31, 10015, '/sys/base/role', 'sys/base/role', 'IdcardOutlined', '角色', 10, true, '2026-04-21 10:54:03.230143', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000013, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413196675798462464, 'file_config', '存储配置', 1, 470000000000000005, 31, 413196675756519424, '/sys/base/file-config', 'sys/base/file-config', 'DatabaseOutlined', NULL, 30, true, '2026-05-16 17:57:04.333267', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000008, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (441000000000000010, 'slow_sql_monitoring', '慢 SQL 分析', 1, 470000000000000013, 30, 441000000000000001, '/sys/monitor/slow-sql', 'sys/monitor/slow-sql', 'DatabaseOutlined', '查看指定应用实例的 Druid SQL 内存聚合统计', 10, true, '2026-08-10 17:22:51.574039', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000024, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (421000000000000010, 'cache', '缓存管理', 1, 470000000000000014, 30, 50070, '/sys/monitor/cache-management', 'sys/monitor/cache-management', 'DatabaseOutlined', '统一查看和操作本地与 Redis 缓存', 10, true, '2026-08-03 21:22:35.436359', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000020, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (426000000000000011, 'script', '脚本管理', 1, 2096233875852730369, 30, 419000000000000002, '/sys/monitor/script-manage', 'sys/monitor/script-manage', 'CodeOutlined', '维护可复用的运维脚本', 20, true, '2026-08-05 00:25:49.513377', '2026-09-05 21:49:01.755879', NULL, 1, 3, 450000000000000022, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000001201, 'email_account', '发信账号', 1, 470000000000001200, 470000000000001000, 470000000000001101, '/sys/message/email-account', 'sys/message/email-account', 'MailOutlined', '维护 SMTP 发信账号', 10, true, '2026-08-22 13:42:50.92663', '2026-08-22 14:19:00.201094', NULL, NULL, 1, 470000000000001010, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000001203, 'email_record', '发送记录', 1, 470000000000001200, 470000000000001000, 470000000000001121, '/sys/message/email-record', 'sys/message/email-record', 'HistoryOutlined', '查看邮件投递状态和尝试结果', 30, true, '2026-08-22 13:42:50.92663', '2026-08-22 14:19:00.201094', NULL, NULL, 1, 470000000000001030, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000001202, 'email_compose', '发送邮件', 1, 470000000000001200, 470000000000001000, 470000000000001111, '/sys/message/email-compose', 'sys/message/email-compose', 'FormOutlined', '创建正式邮件投递任务', 20, true, '2026-08-22 13:42:50.92663', '2026-09-05 21:50:58.059558', NULL, 1, 2, 470000000000001020, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000001, 'organization_and_user', '组织与用户', 0, 0, 31, 10030, NULL, NULL, 'TeamOutlined', '组织架构与用户账号', 10, true, '2026-08-19 16:16:00.874373', '2026-08-19 16:16:00.874373', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000002, 'role_and_permission', '角色与权限', 0, 0, 31, 10030, NULL, NULL, 'SafetyOutlined', '角色、权限定义与授权关系', 20, true, '2026-08-19 16:16:00.874373', '2026-08-19 16:16:00.874373', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (520000000000000402, 'openapi_catalog', 'API 文档', 1, 520000000000000400, 31, 520000000000000201, '/sys/base/openapi-catalog', 'sys/base/openapi-catalog', 'FileTextOutlined', '查看 API 版本、协议与报文结构', 20, true, '2026-08-31 21:18:52.196357', '2026-08-31 21:18:52.196357', NULL, NULL, 0, 520000000000000020, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (520000000000000403, 'openapi_invocation', '调用监控', 1, 520000000000000400, 31, 520000000000000301, '/sys/base/openapi-invocation', 'sys/base/openapi-invocation', 'LineChartOutlined', '查看调用日志和统计', 30, true, '2026-08-31 21:18:52.196357', '2026-08-31 21:18:52.196357', NULL, NULL, 0, 520000000000000030, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (520000000000000401, 'openapi_application', '第三方应用', 1, 520000000000000400, 31, 520000000000000101, '/sys/base/openapi-application', 'sys/base/openapi-application', 'PieChartOutlined', '维护调用方、访问策略、凭据和授权', 10, true, '2026-08-31 21:18:52.196357', '2026-09-05 21:39:41.855314', NULL, 1, 1, 520000000000000010, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (490000000000000301, 'monitor_alert', '监控告警', 1, 470000000000000011, 30, 490000000000000101, '/sys/monitor/alert', 'sys/monitor/alert', 'BellOutlined', '告警规则与事件', 20, true, '2026-08-25 14:45:23.512402', '2026-09-05 21:44:43.459121', NULL, 1, 1, 450000000000000041, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (419000000000000011, 'script_console', '脚本控制台', 1, 2096233875852730369, 30, 419000000000000001, '/sys/monitor/script-console', 'sys/monitor/script-console', 'ConsoleSqlOutlined', '执行受控的服务端 JavaScript 运维脚本', 10, true, '2026-05-19 14:17:47.759591', '2026-09-05 21:48:22.577579', NULL, 1, 3, 450000000000000022, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (2096233875852730369, 'script_operations', '脚本运维', 0, 0, 30, 10020, NULL, NULL, 'CodeOutlined', '脚本运维能力', 50, true, '2026-09-05 21:47:56.803247', '2026-09-05 21:48:37.57165', 1, 1, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000004, 'platform_structure', '平台结构', 0, 0, 31, 10030, NULL, NULL, 'ClusterOutlined', '领域、应用、功能与菜单结构', 30, true, '2026-08-19 16:16:00.874373', '2026-09-05 22:04:00.76592', NULL, 1, 2, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000005, 'system_configuration', '系统配置', 0, 0, 31, 10030, NULL, NULL, 'SettingOutlined', '系统级参数、界面、存储与附件配置', 40, true, '2026-08-19 16:16:00.874373', '2026-09-05 22:04:31.936114', NULL, 1, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (520000000000000400, 'openapi_platform', '开放平台', 0, 0, 31, 10030, NULL, NULL, 'ApiOutlined', '第三方应用、API 文档与调用监控', 50, true, '2026-08-31 21:18:52.196357', '2026-09-05 22:04:39.993337', NULL, 1, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000003, 'data_and_numbering', '资料与编号', 0, 0, 31, 10030, NULL, NULL, 'ProfileOutlined', '通用基础资料与编号规则', 60, true, '2026-08-19 16:16:00.874373', '2026-09-05 22:04:48.172581', NULL, 1, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000001200, 'email_service', '邮件服务', 0, 0, 470000000000001000, 470000000000001100, NULL, NULL, 'MailOutlined', 'SMTP 发信账号、邮件发送与投递记录', 10, true, '2026-08-22 14:19:00.201094', '2026-08-22 14:19:00.201094', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413260828571553792, 'execution', '执行记录', 1, 0, 32, 413260828550582272, '/sys/scheduler/execution', 'sys/scheduler/execution', 'HistoryOutlined', NULL, 20, true, '2026-05-16 22:11:59.546207', '2026-08-19 16:35:38.652014', NULL, NULL, 3, 450000000000000029, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (430000000000000020, 'purchase_requisition', '采购申请', 1, 0, 430000000000000002, 430000000000000010, '/scm/procurement/purchase-requisition', 'scm/procurement/purchase-requisition', 'FileAddOutlined', '采购申请单', 10, true, '2026-07-27 17:59:01.999094', '2026-08-19 16:35:38.652014', NULL, NULL, 3, 450000000000000002, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (411644663089963008, 'basic_data', '基础资料', 1, 470000000000000003, 31, 411644662943162368, 'sys/base/basic-data', 'sys/base/basic-data', 'ProfileOutlined', NULL, 10, true, '2026-05-12 11:09:55.668845', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000006, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (420000000000001104, 'attachment_config', '附件配置', 1, 470000000000000005, 31, 420000000000001102, '/sys/base/attachment-config', 'sys/base/attachment-config', 'PaperClipOutlined', '统一管理附件上传限制和临时附件有效期', 40, true, '2026-08-06 23:55:26.160595', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000005, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (450000000000000110, 'feature', '功能管理', 1, 470000000000000004, 31, 450000000000000101, '/sys/base/feature', 'sys/base/feature', 'ClusterOutlined', '维护系统功能目录的运营字段', 30, true, '2026-08-12 14:52:21.701053', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000100, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413501707345334272, 'runtime_monitor', '运行监控', 1, 470000000000000011, 30, 413501707269836800, '/sys/monitor/runtime', 'sys/monitor/runtime', 'DashboardOutlined', '查看主机与应用实例实时状态及历史趋势', 10, true, '2026-05-17 14:09:09.521627', '2026-08-25 14:45:23.512402', NULL, NULL, 4, 450000000000000021, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (460000000000000020, 'number_rule', '编号规则', 1, 470000000000000003, 31, 460000000000000011, '/sys/base/number-rule', 'sys/base/number-rule', 'FieldNumberOutlined', '维护系统编号规则', 20, true, '2026-08-15 23:23:08.073506', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 460000000000000010, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413172783545511936, 'ui_config', '界面配置', 1, 470000000000000005, 31, 413172783499374592, '/sys/base/ui-config', 'sys/base/ui-config', 'MonitorOutlined', NULL, 20, true, '2026-05-16 16:22:07.976623', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000014, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000011, 'runtime_monitoring', '运行监控', 0, 0, 30, 10020, NULL, NULL, 'DashboardOutlined', '应用实例与缓存运行状态', 10, true, '2026-08-19 16:16:00.874373', '2026-08-19 16:16:00.874373', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000012, 'audit_logs', '审计日志', 0, 0, 30, 10020, NULL, NULL, 'AuditOutlined', '登录与业务操作审计记录', 20, true, '2026-08-19 16:16:00.874373', '2026-08-19 16:16:00.874373', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000013, 'diagnostic_analysis', '诊断分析', 0, 0, 30, 10020, NULL, NULL, 'ToolOutlined', '慢 SQL 与线程运行诊断', 30, true, '2026-08-19 16:16:00.874373', '2026-08-19 16:16:00.874373', NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (470000000000000014, 'data_operations', '数据运维', 0, 0, 30, 10020, NULL, NULL, 'DatabaseOutlined', '缓存、数据库运维能力', 40, true, '2026-08-19 16:16:00.874373', '2026-09-05 21:45:59.52236', NULL, 1, 3, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (426000000000000012, 'script_execution_log', '脚本执行记录', 1, 2096233875852730369, 30, 426000000000000001, '/sys/monitor/script-log', 'sys/monitor/script-log', 'HistoryOutlined', '查看脚本控制台执行审计', 30, true, '2026-08-05 00:25:49.513377', '2026-09-05 21:48:48.459868', NULL, 1, 3, 450000000000000023, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (2104, 'permission', '权限定义', 1, 470000000000000002, 31, 10014, '/sys/base/permission', 'sys/base/permission', 'SafetyOutlined', '权限', 20, true, '2026-04-14 13:59:27.544725', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000012, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (50061, 'sys_param', '系统参数', 1, 470000000000000005, 31, 50051, '/sys/base/sys-param', 'sys/base/sys-param', 'FormOutlined', NULL, 10, true, '2026-05-17 01:13:51.334518', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000011, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413501707391471616, 'thread_diagnostic', '线程诊断', 1, 470000000000000013, 30, 438000000000000001, '/sys/monitor/thread', 'sys/monitor/thread', 'ToolOutlined', '选择在线实例并查看线程、堆栈、热点和死锁信息', 20, true, '2026-05-17 14:09:09.53259', '2026-08-19 16:16:00.874373', NULL, NULL, 3, 450000000000000027, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (3002, 'login_log', '登录日志', 1, 470000000000000012, 30, 10022, '/sys/monitor/login-log', 'sys/monitor/login-log', 'FileTextOutlined', '登录日志', 10, true, '2026-04-22 13:47:23.710312', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000017, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (2102, 'user', '用户管理', 1, 470000000000000001, 31, 10012, '/sys/base/user', 'sys/base/user', 'UserOutlined', '用户', 20, true, '2026-04-14 13:59:27.544725', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000015, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (3103, 'app', '应用管理', 1, 470000000000000004, 31, 10035, '/sys/base/app', 'sys/base/app', 'AppstoreOutlined', '应用管理', 20, true, '2026-04-22 18:06:56.092765', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000004, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (50081, 'cache_status', '缓存状态', 1, 470000000000000011, 30, 50070, '/sys/monitor/cache-status', 'sys/monitor/cache-status', 'LineChartOutlined', 'Redis 运行状态与 JetCache 实时统计', 20, true, '2026-05-17 20:09:21.311371', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000020, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413501707400000002, 'sql_console', 'SQL 控制台', 1, 470000000000000014, 30, 413501707400000001, '/sys/monitor/sql-console', 'sys/monitor/sql-console', 'ConsoleSqlOutlined', '执行 PostgreSQL 查询、单条命令或批量 INSERT', 20, true, '2026-05-18 00:28:23.884474', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000025, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (413501707400000004, 'sql_execution_log', 'SQL 执行记录', 1, 470000000000000014, 30, 413501707400000003, '/sys/monitor/sql-log', 'sys/monitor/sql-log', 'HistoryOutlined', '查看 SQL 控制台执行审计', 30, true, '2026-05-18 00:43:01.026314', '2026-08-19 16:16:00.874373', NULL, NULL, 1, 450000000000000026, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (3003, 'operation_log', '操作日志', 1, 470000000000000012, 30, 10023, '/sys/monitor/operate-log', 'sys/monitor/operate-log', 'AuditOutlined', '操作日志', 20, true, '2026-04-22 13:47:23.710312', '2026-08-19 16:16:00.874373', NULL, 1, 1, 450000000000000018, 'INTERNAL_PAGE', NULL, NULL);
INSERT INTO public.t_sys_menu VALUES (3102, 'domain', '领域管理', 1, 470000000000000004, 31, 10031, '/sys/base/domain', 'sys/base/domain', 'CloudOutlined', '领域目录管理', 10, true, '2026-04-22 18:06:56.092765', '2026-08-20 16:06:19.886981', NULL, 1, 2, 450000000000000007, 'INTERNAL_PAGE', NULL, NULL);


--
-- Data for Name: t_sys_monitor_alert_rule; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000001, 'HOST_CPU_HIGH', '主机 CPU 使用率过高', 'HOST', true, 'WARNING', 0.900000, 300, 0.800000, 1800, false, 'CPU 使用率持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATIO', '%', 0.000000, 1.000000, 0.900000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000002, 'HOST_MEMORY_HIGH', '主机内存使用率过高', 'HOST', true, 'WARNING', 0.900000, 300, 0.800000, 1800, false, '物理内存使用率持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATIO', '%', 0.000000, 1.000000, 0.900000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000003, 'HOST_SWAP_HIGH', '主机交换空间使用率过高', 'HOST', true, 'WARNING', 0.800000, 300, 0.700000, 1800, false, '交换空间使用率持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATIO', '%', 0.000000, 1.000000, 0.900000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000004, 'HOST_DISK_HIGH', '主机文件系统使用率过高', 'HOST', true, 'CRITICAL', 0.900000, 300, 0.850000, 1800, false, '任一重要文件系统持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATIO', '%', 0.000000, 1.000000, 0.900000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000005, 'INSTANCE_HEAP_HIGH', '实例堆内存使用率过高', 'INSTANCE', true, 'WARNING', 0.900000, 300, 0.800000, 1800, false, 'JVM 堆内存使用率持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATIO', '%', 0.000000, 1.000000, 0.900000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000011, 'DB_POOL_HIGH', '数据库连接池使用率过高', 'INSTANCE', true, 'WARNING', 0.900000, 300, 0.800000, 1800, false, '数据库连接池使用率持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATIO', '%', 0.000000, 1.000000, 0.900000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000006, 'INSTANCE_BLOCKED_THREADS', '实例阻塞线程过多', 'INSTANCE', true, 'WARNING', 5.000000, 120, 1.000000, 1800, false, '阻塞线程数持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'COUNT', '个', 0.000000, NULL, 1.000000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000012, 'DB_POOL_WAITING', '数据库连接池存在等待', 'INSTANCE', true, 'CRITICAL', 1.000000, 60, 0.000000, 1800, false, '连接池等待线程持续存在', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'COUNT', '个', 0.000000, NULL, 1.000000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000008, 'HTTP_ERROR_RATE_HIGH', 'HTTP 5xx 速率过高', 'INSTANCE', true, 'CRITICAL', 1.000000, 300, 0.200000, 1800, false, 'HTTP 5xx 每秒速率持续超过阈值', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'RATE', 'req/s', 0.000000, NULL, 1.000000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000009, 'HTTP_LATENCY_HIGH', 'HTTP P95 延迟过高', 'INSTANCE', true, 'WARNING', 1000.000000, 300, 800.000000, 1800, false, 'HTTP P95 延迟持续超过阈值（毫秒）', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'DURATION_MS', 'ms', 0.000000, NULL, 1000.000000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000007, 'INSTANCE_OFFLINE', '应用实例离线', 'INSTANCE', true, 'CRITICAL', 1.000000, 30, 0.000000, 1800, false, '持久化目录中的实例心跳过期', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'BOOLEAN', '', 0.000000, 1.000000, 1.000000);
INSERT INTO public.t_sys_monitor_alert_rule VALUES (490000000000000013, 'REDIS_HEALTH_DOWN', 'Redis 健康检查失败', 'INSTANCE', true, 'CRITICAL', 1.000000, 30, 0.000000, 1800, false, 'Redis 健康状态异常', 0, '2026-08-25 14:45:23.512402+08', '2026-08-25 14:45:23.512402+08', NULL, NULL, 'BOOLEAN', '', 0.000000, 1.000000, 1.000000);


--
-- Data for Name: t_sys_number_reference; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_number_reference VALUES (461000000000000001, 'scm/procurement/purchase-requisition.number', 450000000000000002, '采购申请编号', 'scm/procurement/purchase-requisition', true, '采购申请业务编号', NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_number_reference VALUES (461000000000000002, 'sys/base/basic-data-item.number', 450000000000000006, '基础资料编码', 'sys/base/basic-data-item', true, '基础资料节点编码', NULL, NULL, NULL, NULL, 0);


--
-- Data for Name: t_sys_number_rule_segment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_number_rule_segment VALUES (461000000000000011, 'scm/procurement/purchase-requisition', 1, 'FIXED', 'PR', NULL, NULL, '-');
INSERT INTO public.t_sys_number_rule_segment VALUES (461000000000000012, 'scm/procurement/purchase-requisition', 2, 'DATE', 'bill.bizDate', 'yyyyMMdd', NULL, '-');
INSERT INTO public.t_sys_number_rule_segment VALUES (461000000000000013, 'scm/procurement/purchase-requisition', 3, 'SEQUENCE', NULL, NULL, 5, '');
INSERT INTO public.t_sys_number_rule_segment VALUES (2094999605954486274, 'sys/base/basic-data-item', 1, 'FIXED', 'BD', NULL, NULL, '-');
INSERT INTO public.t_sys_number_rule_segment VALUES (2094999605962874881, 'sys/base/basic-data-item', 2, 'SEQUENCE', NULL, NULL, 4, '');


--
-- Data for Name: t_sys_openapi_release; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_openapi_release VALUES (520000000000000001, 'sys.basic-data.items', 'v1', 'sys.basicData.items.queryByCategory', '按分类获取基础数据信息', 'POST', '/openapi/sys/base/basic-data/v1/items/query', 'PUBLISHED', '返回分类及全部祖先均启用的叶子资料，不暴露内部数据库主键。', '{"type": "object", "required": ["categoryNumber"], "properties": {"categoryNumber": {"type": "string", "maxLength": 100}}}', '{"type": "object", "properties": {"items": {"type": "array", "items": {"type": "object", "properties": {"name": {"type": "string"}, "number": {"type": "string"}, "namePath": {"type": "string"}, "numberPath": {"type": "string"}, "parentNumber": {"type": ["string", "null"]}}}}, "categoryNumber": {"type": "string"}}}', '请求明文：`{"categoryNumber":"分类编码"}`。响应仅包含稳定业务编码、名称和路径。', true, '2026-08-31 21:18:52.196357', NULL, NULL, NULL, 0, 'sys', '系统管理', 'base', '基础平台', 'basic-data', '基础资料', '{"categoryNumber": "分类编码"}', '{"items": [{"name": "资料名称", "number": "资料编码", "namePath": "分类名称/资料名称", "numberPath": "分类编码/资料编码", "parentNumber": null}], "categoryNumber": "分类编码"}');


--
-- Data for Name: t_sys_org; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_org VALUES (1, 'SM有限公司', 'SM', NULL, 1, '2026-04-14 13:15:18.817269', '2026-08-12 17:12:51.329632', NULL, NULL, 'SM', 'SM有限公司', 'COMPANY', true, false, NULL, NULL, 2);
INSERT INTO public.t_sys_org VALUES (2087035058459361282, '领导层', '101', 1, 2, '2026-08-11 12:35:07.831649', '2026-08-12 17:12:51.329632', 1, NULL, 'SM/101', 'SM有限公司/领导层', 'DEPARTMENT', true, false, NULL, NULL, 1);
INSERT INTO public.t_sys_org VALUES (2087035439688040449, '财务部', '102', 1, 3, '2026-08-11 12:36:38.723212', '2026-08-12 17:12:51.329632', 1, NULL, 'SM/102', 'SM有限公司/财务部', 'DEPARTMENT', true, false, NULL, NULL, 1);
INSERT INTO public.t_sys_org VALUES (2096228918898434050, '销售部', '103', 1, 4, '2026-09-05 21:28:14.973521', '2026-09-05 21:28:14.968869', 1, NULL, 'SM/103', 'SM有限公司/销售部', 'DEPARTMENT', true, false, NULL, NULL, 0);


--
-- Data for Name: t_sys_param; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_param VALUES (425000000000000010, 'SQL_CONSOLE_MAX_ROWS', 'SQL 控制台最大返回行数', '1000', '允许范围 1～5000；超过限制的查询结果会被截断', '2026-08-04 22:10:07.527334', NULL, NULL, NULL, true, 0, 450000000000000025);
INSERT INTO public.t_sys_param VALUES (426000000000000010, 'SCRIPT_CONSOLE_TIMEOUT_SECONDS', '脚本控制台超时秒数', '30', '允许范围 1～300 秒；超时将取消 JavaScript 并回滚原子事务', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, true, 0, 450000000000000022);
INSERT INTO public.t_sys_param VALUES (426000000000000011, 'SCRIPT_CONSOLE_MAX_SOURCE_LENGTH', '脚本控制台最大源码长度', '100000', '允许范围 1000～1000000 字符', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, true, 0, 450000000000000022);
INSERT INTO public.t_sys_param VALUES (426000000000000012, 'SCRIPT_CONSOLE_MAX_OUTPUT_LENGTH', '脚本控制台最大输出长度', '100000', '允许范围 1000～1000000 字符；超过限制的输出将被截断', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, true, 0, 450000000000000022);
INSERT INTO public.t_sys_param VALUES (480000000000000001, 'LOGIN_CAPTCHA_CHALLENGE_EXPIRE_SECONDS', '滑块挑战有效秒数', '120', '必须为正整数；建议 60～300 秒，过短会影响操作，过长会扩大挑战重放窗口', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000002, 'LOGIN_CAPTCHA_TICKET_EXPIRE_SECONDS', '滑块票据有效秒数', '90', '必须为正整数；建议 30～180 秒，票据只能消费一次', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000003, 'LOGIN_CAPTCHA_MIN_INTERVAL_MILLIS', '同一IP获取滑块最小间隔毫秒数', '1000', '必须为正整数；建议 500～5000 毫秒，用于限制高频图片生成', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000004, 'LOGIN_CAPTCHA_IP_MAX_PER_MINUTE', '同一IP每分钟最多创建滑块数', '10', '必须为正整数；内网共享出口可根据实际并发适当调高', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000005, 'LOGIN_FAILURE_WINDOW_MINUTES', '登录失败统计窗口分钟数', '10', '必须为正整数；账号、IP及账号IP组合共用该统计窗口', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000006, 'LOGIN_ACCOUNT_MAX_FAILURES', '账号失败触发短时保护次数', '10', '必须为正整数；达到后进入短时账号保护，避免设置过低造成恶意阻断', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000007, 'LOGIN_ACCOUNT_BLOCK_SECONDS', '账号短时保护秒数', '60', '必须为正整数；建议使用短时保护，禁止配置成长时间账号锁定', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000008, 'LOGIN_IP_MAX_FAILURES', 'IP失败触发保护次数', '30', '必须为正整数；内网共享出口应使用高于账号IP组合的阈值', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000009, 'LOGIN_IP_BLOCK_MINUTES', 'IP保护分钟数', '5', '必须为正整数；共享出口环境修改前应评估同网用户影响', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000010, 'LOGIN_ACCOUNT_IP_MAX_FAILURES', '账号IP组合失败触发保护次数', '5', '必须为正整数；用于优先限制单一来源对单一账号的连续尝试', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000011, 'LOGIN_ACCOUNT_IP_BLOCK_MINUTES', '账号IP组合保护分钟数', '10', '必须为正整数；只限制当前账号与当前客户端IP组合', '2026-08-21 22:29:17.700001', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000012, 'PASSWORD_EMAIL_CODE_EXPIRE_MINUTES', '邮箱改密验证码有效分钟数', '10', '必须为正整数；建议 5～15 分钟，验证码只能成功消费一次', '2026-08-27 17:45:20.015544', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000013, 'PASSWORD_EMAIL_CODE_RESEND_SECONDS', '邮箱改密验证码重发间隔秒数', '60', '必须为正整数；限制同一账号和客户端短时间重复发送', '2026-08-27 17:45:20.015544', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (480000000000000014, 'PASSWORD_EMAIL_CODE_MAX_ATTEMPTS', '邮箱改密验证码最大尝试次数', '5', '必须为正整数；达到次数后验证码立即失效', '2026-08-27 17:45:20.015544', NULL, NULL, NULL, true, 0, 480000000000000100);
INSERT INTO public.t_sys_param VALUES (520000000000000001, 'INBOX_POLL_INTERVAL_SECONDS', '消息轮询间隔（秒）', '60', '0关闭轮询；启用时为10～2147483秒的整数，默认60秒。配置随下次刷新生效；关闭后可重新打开消息侧栏或聚焦窗口读取新配置。', '2026-09-04 00:08:09.552499', NULL, NULL, NULL, true, 0, NULL);


--
-- Data for Name: t_sys_role; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_role_perms; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_script; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_ui_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_ui_config VALUES (2085385490455228417, 'Smart Manage', NULL, NULL, 'Smart Manage', NULL, '2026-08-06 23:20:20.177898', '2026-08-28 13:24:27.40603', 1, 1, 49, NULL, NULL, NULL, false, NULL, true, false, false, true, false, 100, 100, 12);


--
-- Data for Name: t_sys_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_user VALUES (1, 'administrator', '$argon2i$v=19$m=65536,t=2,p=1$YX7MmbacZUT02bWnUBFzLQ$yhCo/keSWZm4rO7TQ60+9WsnaQoXNWM7/I6TYGXlQgw', NULL, '18888888888', '#276FF5', true, '2026-07-27 17:59:01.985745', NULL, '2026-09-05 18:42:13.003955', 1, 39, true, '管理员', 'administrator', NULL, NULL, NULL, NULL, 0);


--
-- Data for Name: t_sys_user_assignment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_user_assignment VALUES (2088114309847031809, 1, 1, '系统管理员', false, true, '2026-08-14 12:03:41.414395', 1, '2026-08-14 12:03:41.356437', NULL);


--
-- Data for Name: t_sys_user_role; Type: TABLE DATA; Schema: public; Owner: -
--



--

--
-- 循环引用约束必须在初始化数据写入后建立，避免依赖超级用户关闭触发器。
ALTER TABLE ONLY public.t_sys_basic_data_item
    ADD CONSTRAINT fk_basic_data_item_parent FOREIGN KEY (parent_id) REFERENCES public.t_sys_basic_data_item(id);

ALTER TABLE ONLY public.t_sys_number_reference
    ADD CONSTRAINT fk_sys_number_reference_default_rule FOREIGN KEY (default_rule_key) REFERENCES public.t_sys_number_rule(rule_key);

ALTER TABLE ONLY public.t_sys_number_rule
    ADD CONSTRAINT fk_sys_number_rule_reference FOREIGN KEY (reference_key) REFERENCES public.t_sys_number_reference(reference_key);

ALTER TABLE ONLY public.t_sys_org
    ADD CONSTRAINT fk_sys_org_parent FOREIGN KEY (parent_id) REFERENCES public.t_sys_org(id);

--
-- PostgreSQL database dump complete
--

-- 弱口令词库与系统配置菜单的最终初始化数据。
INSERT INTO public.t_sys_feature (id, feature_key, app_id, default_name, default_seq, description)
VALUES (560000000000000100, 'sys/base/weak-password', 31, '弱口令管理', 135, '维护设置密码时拒绝使用的本地弱口令词条');
INSERT INTO public.t_sys_permission (id, name, number, feature_id) VALUES
(560000000000000101, '弱口令管理-查询', 'sys:base:weak-password:listPage', 560000000000000100),
(560000000000000102, '弱口令管理-详情', 'sys:base:weak-password:detail', 560000000000000100),
(560000000000000103, '弱口令管理-保存', 'sys:base:weak-password:save', 560000000000000100),
(560000000000000104, '弱口令管理-删除', 'sys:base:weak-password:delete', 560000000000000100);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, sort, feature_id, target_type, icon)
VALUES (560000000000000110, 'weak_password', '弱口令管理', 1, 470000000000000005, 31, 560000000000000101,
'/sys/base/weak-password', 'sys/base/weak-password', 60, 560000000000000100, 'INTERNAL_PAGE', 'LockOutlined');

INSERT INTO public.t_sys_weak_password (id, word, match_digest, description) VALUES
(560000000000010000, '......', '895361ffbf88d5a8c40d47f8dd3d03df2042ca9360d08738bcbb4faa39495404', NULL),
(560000000000010001, '@163.com', '20f2bc26d5527954f2d2747a5e31b10f7401ff2d76a8ec18feeb23a559c45a30', NULL),
(560000000000010002, '******', '2efb1047074f7a387fa60c82d2b05bc742cfaf8163ccbb2012cb61108f87fa4f', NULL),
(560000000000010003, '&nbsp', 'f158da0ce8398e51d025dfff826c615ad42ff6f54913000799aefc6b146b433f', NULL),
(560000000000010004, '0.0.0.', '9d6112f815bc4c8191c5f4ca3f761d45dfd72ed37dcb17d846ae5b20f148639f', NULL),
(560000000000010005, '0.123456', '4339722f73413c86a2a152c3164c0188ce346bc95d107808ecc75c0ba136980c', NULL),
(560000000000010006, '000...', '1ac29c742b4e0d6b5d492b57b727b9ed621f4894407783514d6b46793f896c61', NULL),
(560000000000010007, '0000', '9af15b336e6a9619928537df30b2e6a2376569fcf9d7e773eccede65606529a0', NULL),
(560000000000010008, '00000', 'e7042ac7d09c7bc41c8cfa5749e41858f6980643bc0db1a83cc793d3e24d3f77', NULL),
(560000000000010009, '000000', '91b4d142823f7d20c5f08df69122de43f35f057a988d9619f6d3138485c9a203', NULL),
(560000000000010010, '000000.', '5f2c24ad3cc238bec6d1b7cb7821cffd6473b93920ca4f30785a7865fe138dbe', NULL),
(560000000000010011, '0000000', '20fdf64da3cd2c78ec3c033d2ac628bacf701711fa99435ee37bef0304800dc5', NULL),
(560000000000010012, '00000000', '7e071fd9b023ed8f18458a73613a0834f6220bd5cc50357ba3493c6040a9ea8c', NULL),
(560000000000010013, '000000000', 'f120bb5698d520c5691b6d603a00bfd662d13bf177a04571f9d10c0745dfa2a5', NULL),
(560000000000010014, '0000000000', '84d9c4b849506b6d8f8075a9000e7e0a254be71060ea889fad3c88395988f4fc', NULL),
(560000000000010015, '000000a', 'e8fa3958544bd139dc4fae26f725de7fd8394deb3b00c8831451083aee2f6101', NULL),
(560000000000010016, '000111', '4718021fe01050dbde3316fb352f7f00769ba43127b16224fad4485efaa7dae8', NULL),
(560000000000010017, '000123', '4403e0e0dc9196168d76660d9e6fbf9bc12342cae96d8e275e149db921dd40df', NULL),
(560000000000010018, '007007', 'dc6f7e7a3940cf045fc5d85257b4eb290118e410c3f6119ba2bf9a70a4da1c7d', NULL),
(560000000000010019, '010101', '0a1d18a485f77dcee53ea81f1010276b67153b745219afc4eac4288045f5ca3d', NULL),
(560000000000010020, '010203', 'b79ea17b7c5ca8fe9cccd8cdba6e8f8ed0b3c948f9f709ed0f47d2fd47fcba82', NULL),
(560000000000010021, '012345', '2224512ef44a62e580bb1c0dcb33aff688f4e7da8a488aeb4e7ca402c5cacf45', NULL),
(560000000000010022, '0123456', '5f6121bc06e18e209920d57d2f16b17cc82dfc2ade1d375d6951b99c65d1b89d', NULL),
(560000000000010023, '0123456789', '84d89877f0d4041efb6bf91a16f0248f2fd573e6af05c19f96bedb9f882f7882', NULL),
(560000000000010024, '0147258369', '45d1b313760b99f84a784f1b0ec7f0fe85c6aac72d6e6d868dbe649ba8c32633', NULL),
(560000000000010025, '0987654321', '17756315ebd47b7110359fc7b168179bf6f2df3646fcc888bc8aa05c78b38ac1', NULL),
(560000000000010026, '1.23457E+11', 'cb238fc2b0a53516d64aeb18df1bf21563e301a2e45f979b759d476c0c4dada5', NULL),
(560000000000010027, '100000', '3bb78535cc9555ff19fe3556aaa41c78a0a45c64d49ba2bc564507648a8e77a1', NULL),
(560000000000010028, '100100', '7618f66753db7ec069c83ed8c197708e1402396774f60961065addd678933871', NULL),
(560000000000010029, '100200', '0b378c4f890f52055cb340edf238857d2fc51833ad2cbfd5b0ee14c394fd67d0', NULL),
(560000000000010030, '100200300', '6c70d0999b3ebf01e76e81a772ae9d04866bc6eb0bd58c8cb56ffbd999c44c83', NULL),
(560000000000010031, '101010', '2a057642222a878bc360f52f8e1f0dfd2af93196f123269397423155a4ec4884', NULL),
(560000000000010032, '10101010', '237320d509717dc3f0d6bdcd5e8dc8f88f8fd94b06c728c1aaf94118ed34af38', NULL),
(560000000000010033, '1010110', 'f277c742e247af420b1b86e6e07385b203fdc0b8607b6f1aa29d11f9d1447764', NULL),
(560000000000010034, '10161215', '4d3e82452b2b6b7c1d3628fdf1cff850bbfeeccd2ec44d20bfd7b94882e16262', NULL),
(560000000000010035, '10203', '91d29cb4702d2677c21abcbeefe7d75a9caff7761216ac71f8f6fb728a03cef7', NULL),
(560000000000010036, '102030', 'a76b7f25b6ba5ec51bd9fa42f4143b63c2495996e783baa4d9f8459d314f6ad2', NULL),
(560000000000010037, '1029384756', '7a3180ba33c911df44691db25e5e2c83c5cb0a8d16655291345b8accbddf863d', NULL),
(560000000000010038, '110110', '1b527626476d0b34565bc1ed5db94a5afbd946fa618ea3441dd5bae7dc84a97a', NULL),
(560000000000010039, '110110110', 'b4379a31ea667296d05fc0f5e1465ce9a51e6ba98115ba7844cd47b43a475791', NULL),
(560000000000010040, '110112', '4fde1e4a4ee18989cceb26cef174b918204d8e141e00c1203b530f5005ef87d2', NULL),
(560000000000010041, '110119', 'cb7caf73fad1a096b511c0470c351825fcc8bfcbe7c27b24176634ded63fece3', NULL),
(560000000000010042, '110119120', '730e2d4a7c7bcbf97d7ec09df707197d0682e5d96c05ebbcc78df60880a02426', NULL),
(560000000000010043, '110120', '34849f5f22e3d3bba2c581c9e69bd1689e90e475151074e017e885e0c0d28bc6', NULL),
(560000000000010044, '110120119', '11dfcbcc9b2d95572e6c4700243cfa692a8b7624e52ae348dc49487c4b1cefc3', NULL),
(560000000000010045, '110120130', '4b63c5ef82ea7bc273ca1b10c7f3d63f5efd5a7655dbe55316f93825e6da2337', NULL),
(560000000000010046, '111000', '91a80f17981c411f0aa8e5d214e459a5aa34bd096173d171845422aced2506c8', NULL),
(560000000000010047, '1111', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', NULL),
(560000000000010048, '11111', 'd17f25ecfbcc7857f7bebea469308be0b2580943e96d13a3ad98a13675c4bfc2', NULL),
(560000000000010049, '111111', 'bcb15f821479b4d5772bd0ca866c00ad5f926e3580720659cc80d39c9d09802a', NULL),
(560000000000010050, '1111111', '2558a34d4d20964ca1d272ab26ccce9511d880579593cd4c9e01ab91ed00f325', NULL),
(560000000000010051, '11111111', 'ee79976c9380d5e337fc1c095ece8c8f22f91f306ceeb161fa51fecede2c4ba1', NULL),
(560000000000010052, '111111111', '1a5376ad727d65213a79f3108541cf95012969a0d3064f108b5dd6e7f8c19b89', NULL),
(560000000000010053, '1111111111', 'd2d02ea74de2c9fab1d802db969c18d409a8663a9697977bb1c98ccdd9de4372', NULL),
(560000000000010054, '11111111111', '534a4a8eafcd8489af32356d5a7a25f88c70cfe0448539a7c42964c1b897a359', NULL),
(560000000000010055, '111111a', '42c354fdaf80ac7335beb1837086aa8335ba50195bef51e2ca2d38fbf7723225', NULL),
(560000000000010056, '111111q', 'd58193aa6908b7961e14cc98c1e5bde788f83d4885d6869c6a5b8cbba5c9a6b7', NULL),
(560000000000010057, '111112', '2e399d0704eb40aeb3e2321017a4e42f400841c3dc54113e1dea6f46a69f9037', NULL),
(560000000000010058, '1111122222', 'e8fcbceae80a396147347c3051a6cce0884e457e90929699d4de5713ff287d24', NULL),
(560000000000010059, '11112222', '0554a5df02ee12f1ae36a51caaef34a31deb9458a48b629da554a2b322466f4a', NULL),
(560000000000010060, '111222', '92c7d71b95dc6540fc58e891dbe649fe72ae5e93b5f42fd7fbdeefe6cef3e51d', NULL),
(560000000000010061, '111222333', 'da5511d2baa83c2e753852f1f2fba11003ed0c46c96820c7589b243a8ddb787a', NULL),
(560000000000010062, '111222tianya', 'eda9bfb9d5e1819d0a2691c3eb77530d0e25dc5414c85445f688eba35a90c9d8', NULL),
(560000000000010063, '111aaa', 'b79a1a1aee388c9e9d12b7fc442f51a918a7034277c3ac402b6c505ecdd7bc74', NULL),
(560000000000010064, '111qqq', '4cd1833da17c8036c9bfbbedb9bb06018f3a6e547a55671aff5d6f1c9292f4f6', NULL),
(560000000000010065, '112112', 'c899ed0bd28dd27b2db3326cdba134627a4f32ee642ce52a03e3b71a62b4b029', NULL),
(560000000000010066, '112211', '2c4dca7c5a34feb03d70447f026e9abb304cf2571e0d98a60cf937e5ff5f1512', NULL),
(560000000000010067, '112233', 'e0bc60c82713f64ef8a57c0c40d02ce24fd0141d5cc3086259c19b1e62a62bea', NULL),
(560000000000010068, '1122334', '75025484543da0cbed8885f19e77675bc18aa3d76cb3c53c738c2109c2629ca7', NULL),
(560000000000010069, '11223344', '4f9f10b304cfe9b2b11fcb1387f694e18f08ea358c7e9f567434d3ad6cbd7fc4', NULL),
(560000000000010070, '1122334455', '9260f889a03c3de5a806b802afdcca308513328a90c44988955d8dc13dd93504', NULL),
(560000000000010071, '112233445566', '5d5a70a2d78879834db3583f8d275582ece06eae78db0d400dda5a08957c6074', NULL),
(560000000000010072, '112358', '00390de2b7074071bb6494e818e84884ef6331ceb0b1e70948bde3ef4ba57b92', NULL),
(560000000000010073, '11235813', 'd4c363025fb95b88563b72ac9f9914ba5e04b66d6e6b39591b90fffdd97a5f75', NULL),
(560000000000010074, '1123581321', '12944f22333c049006e2bdbf7b2ea12fdc4569c21f044eb8c4ccf9b558a3e1f7', NULL),
(560000000000010075, '119119', 'f2978ea1ceaef7962f641fec7e7a4f943a821e4a98bb81a70e21419ea839ca7b', NULL),
(560000000000010076, '119911', 'b8e7adefd4d87b9d27ccf9b41d4adccf01e3ef8b31529f74008e7bcb55fbf691', NULL),
(560000000000010077, '120120', 'b0561306856c69ede0844575ac9943e8b9b84cf709a91bcfece97ffdf68cf527', NULL),
(560000000000010078, '121121', '07e2a546c988554d3abe35b9a6abfe0f001cd28f0a66b0be387dbced783749c4', NULL),
(560000000000010079, '121212', '3ea87a56da3844b420ec2925ae922bc731ec16a4fc44dcbeafdad49b0e61d39c', NULL),
(560000000000010080, '12121212', '054e3b308708370ea029dc2ebd1646c498d59d7203c9e1a44cf0484df98e581a', NULL),
(560000000000010081, '121314', '7d824ad37e366f330ef3d3bafb8dc8b18a5b07622e2830eac5966339d98a94b0', NULL),
(560000000000010082, '1213141516', '3f1d3c40178d5858cea2e316b0692a6a3cc6262df02081804691afac371e4aed', NULL),
(560000000000010083, '122333', '27aa85d9579166a014c206c41971b23eadb20ac6a42aca4a88f7e64cf25fa6a2', NULL),
(560000000000010084, '123...', 'c24007b6e63659810fd7e2025c3d56bf1b75dd0b8c4cdaa00b0b08abc20df32f', NULL),
(560000000000010085, '123.123', 'd691ea406e690bc790d66325b6c79c1cbb92248633ba12ffb067ca526cf9423e', NULL),
(560000000000010086, '1230', '22a2fa7d04248931a8853a7714b86546610afd01b2b1841890e979ba7ba6bcae', NULL),
(560000000000010087, '123000', '07090e14e7927e8dbdc6cb674978a89da48a9ffe7db8a289a69c0124ada91b24', NULL),
(560000000000010088, '1230123', 'b022b6945561a98b372fca98b16d5e0cf4ce17cd1e89a75abc15dfe0686e56e7', NULL),
(560000000000010089, '12301230', 'a9ebb948dee3c444a86ff533f15b1d88feb7cb07eff1dd9182c3ca82d42cbb1f', NULL),
(560000000000010090, '1230456', '98d1a58f0f7cd9ea8083459c05f7ce2ba4b3a50209f2bdf506984d754faa8635', NULL),
(560000000000010091, '12312', 'cfae26288bd82e1a97669b7720470cf394e87b0e53bdd7e584055805cc63001f', NULL),
(560000000000010092, '123123', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', NULL),
(560000000000010093, '1231230', 'dd27564ac5d8b5065d5986d0f9e92fb91e71a23f9f5c13e599985c646c078a16', NULL),
(560000000000010094, '12312300', '152661e89c300b66b7399ac4344736540426063c0529d71299931a97578df902', NULL),
(560000000000010095, '12312312', 'fe7ca4ea0a8715222bf5f4d6c6d1bb962ddc1189f072c6c7e2d20688b1083e81', NULL),
(560000000000010096, '123123123', '932f3c1b56257ce8539ac269d7aab42550dacf8818d075f0bdf1990562aae3ef', NULL),
(560000000000010097, '123123123123', 'b822bb93905a9bd8b3a0c08168c427696436cf8bf37ed4ab8ebf41a07642ed1c', NULL),
(560000000000010098, '123123456', '92650041f7b179e5d0bd54fd82e27414e2a20d9d458ef35d592eff9f1427e1b8', NULL),
(560000000000010099, '123123a', '619bf74a84a52a1cb50a025654076dceb92a911c8929d8a9aec158c35ae359db', NULL),
(560000000000010100, '123123aa', '302ad20d4d1e404877b52a397bb35e527c863bd667b08ef2b2f5955d4aa702b2', NULL),
(560000000000010101, '123123q', '760494deb720fc191186556d732c9c82f80c7dbe93a047b514f86e2ab9485a3f', NULL),
(560000000000010102, '123123qaz', '46f745f8e8583eff05d14ccdc176601950f5c7c139b99a582f01730e535a8401', NULL),
(560000000000010103, '123123qq', '7da567684a284991698474dc20d12a57af2234746bf687ebdf9ad4fb8fc5cd90', NULL),
(560000000000010104, '123258', '0a3656dfaa7e97cafe9064d79e08ebf970615f6831850a1aa1c549aeb1466760', NULL),
(560000000000010105, '123321', 'a320480f534776bddb5cdb54b1e93d210a3c7d199e80a23c1b2178497b184c76', NULL),
(560000000000010106, '1233210', '87f9c6c92b6dda638da486234af2b7380cbdfb4f03a1a72da0397664b4b5a35f', NULL),
(560000000000010107, '123321123', '245bc97ec8d9d70c0a8c2c6048e6afea53965f080a86e9bf1b4130bf3b7af432', NULL),
(560000000000010108, '1233211234567', 'f4b9b88ff7684ede0228d4fe86981176d2087e54d475f89642d60e73aa26e9f1', NULL),
(560000000000010109, '123369', '77df1d2d0635b799ecf3c9536a3437eaf1565b8da1e1dd70faa98757aa7881be', NULL),
(560000000000010110, '1234', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', NULL),
(560000000000010111, '12341234', '1718c24b10aeb8099e3fc44960ab6949ab76a267352459f203ea1036bec382c2', NULL),
(560000000000010112, '1234321', '19461b43bbba8a3a4da70703bda96dbc6dcdc2ee78507e34d2bf0f281932fd1f', NULL),
(560000000000010113, '12344321', 'f931c308fc5b60b421c09969912839dff2776957d98b8d2f91c554ed8fc80f78', NULL),
(560000000000010114, '12345', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', NULL),
(560000000000010115, '1234512345', 'e4a0a90e5ac07d5435c6f25c4cf7cc565becb797bb5b83c515bc427ef32a4770', NULL),
(560000000000010116, '123455', 'fc1f09ab08ebdd072ea6da53a5691abcc18c9163b1be1f0921a5adb50e3f5077', NULL),
(560000000000010117, '1234554321', 'eef1394ee3b80d9ce18ebfe618a056e6e0299c12cd19c99c796d482eb52277c6', NULL),
(560000000000010118, '123456', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', NULL),
(560000000000010119, '123456.', '43fae6c11d7632acc6059de1cced9b09a58caaa878071308ad67f32ef6b11691', NULL),
(560000000000010120, '123456..', 'bd9c92e8d3bbf000caf391391325e0fa26b5355b7156218c82cec0db29da18bd', NULL),
(560000000000010121, '123456...', 'd8c72dfb1042e3e9a128733db80fa8d996dadad0ee67d5e42cae8f13147fce55', NULL),
(560000000000010122, '123456+', '2a2a54b7643123c4502c8b61f553aada36d573354923a3990a4f19f0f72c0593', NULL),
(560000000000010123, '1234560', '0f7bfe6859999fd0ee6e4a7b725d466cebebec7ca75ddd7ef0f2e6d648db6d8f', NULL),
(560000000000010124, '12345600', 'd11e44e473e11bb9496febf13fe935fee12c84904a667ef4f379a2a1ed1446f0', NULL),
(560000000000010125, '123456000', '6360f6c0b91cf9c7010c5833ff73690c117109974f97514c6f3b2908da1ff453', NULL),
(560000000000010126, '1234561', '45c4771dcd1cbd65babf3dd8cd70fed56d428fe708183ba1d146f0ad153773d7', NULL),
(560000000000010127, '123456123', 'afa2d124d75612f83135b61d695b839b819bac0b74e324bd7f5f663e2ba198f8', NULL),
(560000000000010128, '123456123456', '958d51602bbfbd18b2a084ba848a827c29952bfef170c936419b0922994c0589', NULL),
(560000000000010129, '123456520', '9c6cfcb59acff3163c708e47aadca5c16f7bb1a9051711e54be8774954bc72f5', NULL),
(560000000000010130, '1234566', '5dc088487fb505024591604c00eadbd8607ea049dc46857eb803b45e205640f6', NULL),
(560000000000010131, '123456654321', '5e4ff8ebb5fe84d8c7968cec08c95e083c5edf12054b1d2eb1aed051e6042c33', NULL),
(560000000000010132, '1234567', '8bb0cf6eb9b17d0f7d22b456f121257dc1254e1f01665370476383ea776df414', NULL),
(560000000000010133, '123456711', 'a2dd0464ccc51440b051d94a2fb72ea49a8e14c040be022a0cd4e2cc50a0b814', NULL),
(560000000000010134, '12345678', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', NULL),
(560000000000010135, '123456789', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225', NULL),
(560000000000010136, '123456789.', 'f766ae4fe0bdbfee9792703feaf2c1f68b08215c4d2c4f75d90b50628caa3934', NULL),
(560000000000010137, '123456789..', '2aa76af2799725c7edc30b22829d2dc4c6feabe568fc59fc60fb62897c94ba8f', NULL),
(560000000000010138, '123456789*', '7dee34a97846a818913b1c3acd1a6af2430bd0f1f5ecafc12fb8aa7011410480', NULL),
(560000000000010139, '123456789+', '7a68eb1b4794f5e5090b8a10cd508076f1c7bc6716c3be497b294e39c629b804', NULL),
(560000000000010140, '1234567890', 'c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646', NULL),
(560000000000010141, '1234567890.', '37a82583eeff5905aa8d96e429cb186aacd44466a14a5a2c3795a84b5404381c', NULL),
(560000000000010142, '12345678900', 'a8476735b37a541a38402a2e7037c79e2d217fe9780e5e34347156ef61eff42b', NULL),
(560000000000010143, '1234567890123', 'bca2b41a2b25e137c83fee346af7bd1e0f52bd560583ca07a1b42f9944c5c50b', NULL),
(560000000000010144, '1234567891', '523aa18ecb892c51fbdbe28c57e10a92419e0a73e8931e578b98baffccf99cdd', NULL),
(560000000000010145, '12345678910', '63640264849a87c90356129d99ea165e37aa5fabc1fea46906df1a7ca50db492', NULL),
(560000000000010146, '123456789123', '2943a567bc05bc66ca6201dbc5f00bec3f774a47b1b94289a2ae8e79834c21a5', NULL),
(560000000000010147, '1234567899', '95234f027b5da5a09928bde5fc432a8a1e525559cab08b830e8bdff74a9db93a', NULL),
(560000000000010148, '123456789a', '2285877fbec4bb1ad6466a6d3596f3a369646db03a02d4f74a2616a50d457a0c', NULL),
(560000000000010149, '123456789aa', 'fe67c5f160ab4be62531b14e25fa319b63a1ac9b0a746aa8df86d2cddedb0b35', NULL),
(560000000000010150, '123456789aaa', 'aee1271eb6f0c04f4e8a27fd86b0b7657caa26fa16013ecf776e7a90518b5fd2', NULL),
(560000000000010151, '123456789ABC', '439087482d21d57584b6d1a26cc9e3844ed75c36f33c0f7952042ab0412b2405', NULL),
(560000000000010152, '123456789asd', 'b10e6427802ea0ab968f3ba5d67e4f9f93a5ed0db6dde16f5dc5ad170fbc9675', NULL),
(560000000000010153, '123456789l', 'c4f6742c9e19625f820a44a1f52693a4c3483ba21ca22fc0fe09e73c7da420d5', NULL),
(560000000000010154, '123456789q', 'd865b65bdb3ba3791fc9549d2e315f3dfbba0128db30316c913795f7d79e8323', NULL),
(560000000000010155, '123456789QQ', 'a804dde5c000a120fc3139fb4c4e33fd67e49f04066c5b74f4395ee10785e7ee', NULL),
(560000000000010156, '123456789qwe', '4346ff92f5b575eaab673ebd7f2723abbdb66a6b15781329e765f310688ca0e9', NULL),
(560000000000010157, '123456789w', '96288ecfa8e8e486a1184f949cb1f97e517ebff4cb5d525618276e599782cfcd', NULL),
(560000000000010158, '123456789z', 'c0a1aeedb65a3baa9b7cccd27a475cae78784d1bd0225706cbd86ecd88ce1f8d', NULL),
(560000000000010159, '12345678a', 'f969248d621bcded4a3582a1c3b17a71eedfefa9120c36ee3bd1957438cd55b9', NULL),
(560000000000010160, '12345679', 'b759803bc6037a05e6564b6447a755b7f3862ba4d0d746785dbe133dcb6c8f4d', NULL),
(560000000000010161, '123456798', '47276f6d8f8ca18f758d2ae311733ff6a46a3eff9944ab9424fba5563c49df3f', NULL),
(560000000000010162, '1234567A', '47ef20207489b775fa4cdcac3c394b517ab22d7460237ae3df1ac0e8963699d6', NULL),
(560000000000010163, '1234568', '07ba38d7a9affba269a613da6d99a7ffa4d128ce38f5e24ee5a7383b796b58b2', NULL),
(560000000000010164, '1234569', '4e6979372fe9f33255d774e1510b488f920cbd452daa7053eb05b7f126a9a80e', NULL),
(560000000000010165, '123456987', 'b4fbe57144df88eee69838fee592dfe7fc250722cbcdb217cc89e67295fd8073', NULL),
(560000000000010166, '123456a', 'f707fdda7c874ff49ebfb2c88a2860c5ff4ce3d94a21efb76566ad0f92c9ad57', NULL),
(560000000000010167, '123456aa', '307dc76cfd3b6c02470771f937d8b87d0e2aa10056fc5750d916ab321ffa3f4f', NULL),
(560000000000010168, '123456aaa', '3704f976a0ac82c4fb8be5743f07a57183a3a095b73ca3ef6f1a710db8720c53', NULL),
(560000000000010169, '123456ab', 'f3ba6d18eccdae368342205bd8d31e9f1d4ca65645067d12c12ed66ef69f85d0', NULL),
(560000000000010170, '123456abc', '931145d4ddd1811be545e4ac88a81f1fdbfaf0779c437efba16b884595274d11', NULL),
(560000000000010171, '123456abcd', '7eb6640227367c8ff142d877496ca3ec5e1c2fc218198fa937054dd69dc6046b', NULL),
(560000000000010172, '123456as', '060f2dd6e6fb4a5167b36ebcbf7f3fc116c417239833b3d82620bb2dc29ed709', NULL),
(560000000000010173, '123456ASD', 'd0403d5a4f922cce1322927f47f64e3ffecccc8b9659390d5ba035c5f45088d7', NULL),
(560000000000010174, '123456l', 'd98458889ed62dc7fb97361c13ee0a1649dfd61b5a30da1aeedfe8820db67b5b', NULL),
(560000000000010175, '123456m', 'de659f7f95f5c6b909d823dc130bfe95e85b3e4e1f1019299e1d6deba318d113', NULL),
(560000000000010176, '123456ok', '8efd83f24f6ff553d7ed6679375aebab367c4917c8372cf33f081df39e9b113f', NULL),
(560000000000010177, '123456q', 'a1e48daec54145146b89d816a089ba3294d2748796b8491e9a719d54d2ca0b8a', NULL),
(560000000000010178, '123456qaz', 'b9537abb5ea2772bac92cc2a65e8dc9fa47607a937adeb599108d1eada06da08', NULL),
(560000000000010179, '123456qq', '3c644eba9a935d9579631a100ef4a120eba6b35e7b9420358edd70f762b3561b', NULL),
(560000000000010180, '123456qw', 'f633b7331e0fb2dbe949cc5d4f038642e37cadde642187c3aca262308addf57a', NULL),
(560000000000010181, '123456qwe', '60b40b7846a7afe973ce089f19068cffc53e40fe28cd076f937c644f6127aad6', NULL),
(560000000000010182, '123456w', 'bc2c4f83690f357367681d5c5ad0d948b14bf66d0a319737820e9575e89cfed7', NULL),
(560000000000010183, '123456z', 'e622afd6a19fa822d1e7462dcd5df695a67710f20f4694e62d1dc10b5ee7f127', NULL),
(560000000000010184, '123456zxc', '093a0b09f3a0e59510931d77e565c8b293e54cbc9da16518305d8be3bbc63c65', NULL),
(560000000000010185, '123456zz', '27c4af017eabc224386c24807f0f927c80290b03b6be3e0ed453e456bc7acf91', NULL),
(560000000000010186, '123457', '54b688a517f7654563a6c64d945a3670880a4c602ec67a065bbebbcd2b22edd5', NULL),
(560000000000010187, '12345a', '1db4a0041876241916ff8b935a46b680de655e06456c77c1d2970688ea2838b9', NULL),
(560000000000010188, '12345qwert', 'cf0b854f5a17fdad773d462438d4d7328722b817d40a74ecb8d9ad79f98aa251', NULL),
(560000000000010189, '123465', '52f1476494897c64f417deb7ef7cd690f1cea9edce638746c420f1240d3d39dc', NULL),
(560000000000010190, '12348765', 'd34115b61acb7d6e14be460183068ef3b292fd734ce9408a84150a4b6d616294', NULL),
(560000000000010191, '1234abcd', '221b37fcdb52d0f7c39bbd0be211db0e1c00ca5fbecd5788780463026c6b964b', NULL),
(560000000000010192, '1234qwer', 'ef51306214d9a6361ee1d5b452e6d2bb70dc7ebb85bf9e02c3d4747fb57d6bec', NULL),
(560000000000010193, '123520', '923ba12cc882ddcc535801c240ab456378210f196417dfdf48f9603dd68646fb', NULL),
(560000000000010194, '123567', 'f7298948225be27bf643ab6ac049c6bf953ab0d6a9513786a49692941142169b', NULL),
(560000000000010195, '123654', '6460662e217c7a9f899208dd70a2c28abdea42f128666a9b78e6c0c064846493', NULL),
(560000000000010196, '123654789', '9e4633d8746b59a6aec1c82f2f7c49fc3e49ac70b6b3803f96752dff8c481af2', NULL),
(560000000000010197, '1236987', '9517ee3c1b2be7f765125e8147a985f10c613a2868f1093d347ec522e97dd48d', NULL),
(560000000000010198, '12369874', '0250af8294d28ac88338a61f40510538c91d126cceedf749596026f27eda438a', NULL),
(560000000000010199, '123698741', '0b2298440845c953a67d4c2a1e8e6cd43616d61498aae4b579461f2cf1bbccb3', NULL),
(560000000000010200, '123698745', 'f7946130bdf34df8177d94eba34f14ea2cffdcae13f5c79c35da3eba4f4dcb36', NULL),
(560000000000010201, '123789', 'b7158b64a98516b31d0c23609f69265a868c594dda5b3c8da9e13159e209c9b6', NULL),
(560000000000010202, '123789456', 'ed6e44a42cc9382a95129b6f00c5ad1e4bcac2a33ab3187ef3c04e2f6444b67c', NULL),
(560000000000010203, '123987', '431cd8c8d5abe5cb5944b0889b32482d85772fbb98987b10fbb7f17110757350', NULL),
(560000000000010204, '123aaa', 'a32df4cf1279c8c1532ef759fbfd1ec7878dd7de07247c8c916f39ea32ec749c', NULL),
(560000000000010205, '123abc', 'dd130a849d7b29e5541b05d2f7f86a4acd4f1ec598c1c9438783f56bc4f0ff80', NULL),
(560000000000010206, '123asd', '049a68c15c0d6e26c8b4a0743e6b87f074864c2fae5983c88956cb2882d608f5', NULL),
(560000000000010207, '123qaz', 'c71df59dfc22b2cfb4c2b54a01479fb8c070668db53fcad55f42639bb33af3d3', NULL),
(560000000000010208, '123qwe', 'fbfb386efea67e816f2dda0a8c94a98eb203757aebb3f55f183755a192d44467', NULL),
(560000000000010209, '123qwe123', 'a72bacd5147d0423b0c55846bd672d409b4c4d3bcbf50fdde1d200525b36303f', NULL),
(560000000000010210, '123qweasd', '85fd7c889f71cf105375595cddc06b9d38fc562cb69c54f8c165aa751d81b3d9', NULL),
(560000000000010211, '123zxc', '429890bd0187ddae1bc7ede50023a9703502216c034a6dac11235d688fc920bb', NULL),
(560000000000010212, '124578', 'e83c1c4388ff0e57a76d5fc7aeb2ae76800665f6a5a2d63fe561c40d26ccd6eb', NULL),
(560000000000010213, '125125', '3b11a134e2e65596f755f62c009da6295349bfb324ac9cffc0c2a8d9e7fdbe32', NULL),
(560000000000010214, '125521', '3358c235aaf40241348d3f410e96043fdbe842b73d754b35bff3d8aede1fd5ad', NULL),
(560000000000010215, '12qwaszx', '7a345ba5e18955831fb1f543443b78bac5a823eeb8d5747e8fcb2c5591b31313', NULL),
(560000000000010216, '130130', '33bda7a0330162eeda1cd55da9e86ed5e9a3ff8cd21b512c92e2c1ca857f1f97', NULL),
(560000000000010217, '1310613106', '4b73fb4e426a8698a6d9305c025569f9359017dba92182181bad7ff8f63e6a0b', NULL),
(560000000000010218, '131131', '9acc9ed8622ff6727507c696e9975c2340bbdf6d55f42f5372763fc83bdab0b9', NULL),
(560000000000010219, '131313', '3fe1f7584833183e2da842b2f18123186919d4aa9828dbebdb3956429d9607bb', NULL),
(560000000000010220, '1314', '3324dab86f4dcdf48ba8ed6d736dcf050f09a23bf617c7d3579224548269ba1f', NULL),
(560000000000010221, '13141314', '3c6e86d57d34af8adb80bfc167597589d80d05b723e01caebfaf7936ffc78a77', NULL),
(560000000000010222, '131415', '788bc9b1b5d0ee575555afd5fa01762fedbf893e6f34b2f5594c24ed4d5d971c', NULL),
(560000000000010223, '1314159', '36fb1e09264e05db44afff1441ba877819e4ea06c105fba43ed348c6bd7e85e8', NULL),
(560000000000010224, '131420', '413c37d7c66ac4fc1a64f2e4833c9ddf9845136f4ab9413b55011631f81b76cd', NULL),
(560000000000010225, '131421', 'f115f14dfd04b9805d3f40d30ce4832b5b1eb8da32449879b124608f700bf207', NULL),
(560000000000010226, '131425', '11041d3e4689886754d28b0c682527717d360d8f1c0a9e4f25b81235d8cbb817', NULL),
(560000000000010227, '1314258', '7cb0b91e8793aca002f0368d3af897e1f54bf7b5d4ac0f0839bf2daf71b62b17', NULL),
(560000000000010228, '131452', 'df32b5516a217f671f146eb6cdd52d35a4aa69427246b732fbb7700cd38a3c0f', NULL),
(560000000000010229, '1314520', '48818c6e6eb2ba468317d76accf24e92dd47e6c09c7db349356fad01d834015e', NULL),
(560000000000010230, '1314520.', 'f56a4df613bb046d429f7080db1a5e5e78d3d07966c77e8f81265a1df459687d', NULL),
(560000000000010231, '13145200', '8c108da2b8eb407de208805675b87243170dfc1ad74152179f6765a59e06be6d', NULL),
(560000000000010232, '131452000', '0cf5992b0aa0d74348e206f9ad8f25d5062ffde37ff87c47af8b550ba2227122', NULL),
(560000000000010233, '1314520123', 'c49c6ca0280685ce2669d29e9523a178228c060fb6f6644167dd4a3f439c365c', NULL),
(560000000000010234, '13145201314520', '8c3ad92aa6162f082953fc237d4885a38c224a334630019d7c19c9521eea3a3a', NULL),
(560000000000010235, '1314520520', 'fd76e0d45bf1bebe4667a59e815cdfe4951991398dbcc80a1e67263f3da2debb', NULL),
(560000000000010236, '1314520a', '4c8d6141993f20f79af69ed18e4c96a1a4f843a70e5305245e942d0a6689a31e', NULL),
(560000000000010237, '1314521', '7214767e6e2b2b951300a272a2c4d2d39ebd9ae87c658d662a683533f3acb28e', NULL),
(560000000000010238, '1314521521', '860654bf0811e3c1c14f6bfeb3792594a97c61f709460ec60032cc2d0700ee2e', NULL),
(560000000000010239, '1314woaini', '33dfae2d7a873c944ce3104eebae2196c7ef606012f7c0202c2da652b0d35468', NULL),
(560000000000010240, '134679', 'c4e4866953bba378413a4558ee984a68ad52e4ce2fa039d4966182fefa632901', NULL),
(560000000000010241, '134679258', '9137c9ef7244bf441841ef56dff455dd4ffe994ccb666e9d697f254791b24abe', NULL),
(560000000000010242, '134679852', 'f40616bfaf4c1e0631d206330ead19b861546d0400b3f9be0589dbadc985ad8e', NULL),
(560000000000010243, '135246', 'cfb789f892fdf2a45ab1bc1f046932df431a7708165393e0f13fab2c9267c0eb', NULL),
(560000000000010244, '135790', 'dec58ab7d7f9fb6bd366cea633274ef3632f8eaa823bf811c14bed255d60e339', NULL),
(560000000000010245, '1357913579', '7a882eafc8ebc133d4e1759925924a320aa67f14aee66ef9e42a864ff3ae908d', NULL),
(560000000000010246, '135792468', '7abe0cd919d5727c83a511ca90b10feface6d747953dd5e772e1ec6d4e6f413a', NULL),
(560000000000010247, '1357924680', '0139ac6fa1fb1ec90bc15fe5eb13421f32579bce849ab697aa3ef3b77823ae17', NULL),
(560000000000010248, '13579246810', 'bddbe74652646e6a12071696f72fa44d6f271bcbe74320988d2c22d0ce7890c0', NULL),
(560000000000010249, '13800138000', 'a6942f9771d67f34034d2f1926988ed3fad3bf1b4e7cedb9a31f31398dea43bc', NULL),
(560000000000010250, '139.com', 'e268f1b5dc1e466e057fed41f8e70c278544676d949ff8bbc9c2993a0c6a2bd8', NULL),
(560000000000010251, '139.com@163.com', '2286fbaadb7c7362f200e3a3fa511cd3879325c0df7570509c0fd1548ea110e0', NULL),
(560000000000010252, '142536', '023f5351b94db0bdcde8dd21da240ac75adc1fc82371c516543b25485cb900de', NULL),
(560000000000010253, '142857', 'ebd72b510911af3e254a030cd891cb804e1902189eee7a0f6199472eb5e4dba2', NULL),
(560000000000010254, '147147', '59d2c86f6f27c1744195cb2ef0bb809fe9b51c7fad22aa3ae9287f1e508705e9', NULL),
(560000000000010255, '147258', '7a2ec40ff8a1247c532309355f798a779e00acff579c63eec3636ffb2902c1ac', NULL),
(560000000000010256, '147258369', '5600715f42bf51c40dc330d750cd996f58fead4ddea56466ce7498d17801b3a5', NULL),
(560000000000010257, '1472583690', '2381e9573a1cb7cbb111f02c81c46e3fb0de4e794e275e60c2f5f8305cb4b1a5', NULL),
(560000000000010258, '147369', 'bc1583dbd69cf369314be86b8579f367cd17e441986f9131c240015829f044cf', NULL),
(560000000000010259, '147852', '0729563253bc11cb72714d61132adfe7ba2346b581b02546c9ac4a65fc0c02d8', NULL),
(560000000000010260, '147852369', '840815f39c15d7ccbe3b5a2a3392eb92294f629cdad004d8354a5e7eb658f356', NULL),
(560000000000010261, '1478963', '61760ab27e09dd222dcfb3a84138566e856e0ae7adf194fdddf0e3803bdbeafb', NULL),
(560000000000010262, '14789632', '595499f88bb033d0e0da3e20b6e9756b7458ab6c22c40fbc394464ecf9610f26', NULL),
(560000000000010263, '147896325', 'fb111a351dfd762ae36a6eee34599163f61102a40fb1cace84d9932d1a0fe636', NULL),
(560000000000010264, '159159', '77081fc6d6f152623855e0c83a8f511c729c5e246193a48babf8c856964eae65', NULL),
(560000000000010265, '159357', 'ba723435a66e490530c3efdfeac868e06fde6e35dcc43fa8528fb1b2c9411ef5', NULL),
(560000000000010266, '159357456', '2129aff312079f361c930f7b3b9051eecac24278860bed26a38ccbc2d712a3c5', NULL),
(560000000000010267, '159753', '3d14c2d4e4ced81e459e4ace7c01466a700000fb94a3bbe944a55fb92693e879', NULL),
(560000000000010268, '159951', 'b77c4e8ab2fb5ce536bb35289abd92d810f235a97032fa7f4701073d8610ae78', NULL),
(560000000000010269, '163.com', 'c1adff28035f67f7337edf46a392df14645e9a3e7774d15027729ac82c5c6be1', NULL),
(560000000000010270, '163163', '422605fa39cbda76b85f09940ef447e653630bad9893d7e2d2ea1ce2fbca1432', NULL),
(560000000000010271, '167669123', '315e7c68f1ad8b74a2c2c9c382b21266044c3bed5049a55fd2fd5886b60a99f4', NULL),
(560000000000010272, '168168', 'f8383e3520d4a9cdd61bb5b50abf75ad88013e3c36c6d07fc0ee2e92b4ff18b4', NULL),
(560000000000010273, '168888', '3303ed88243924540b24feebf4ecf500a98cb0bd43b74735d769a3cc30ba9846', NULL),
(560000000000010274, '16897168', '05e3933cf1de6c07c66b7fc7ba2803e7b60844d2c467614475b30290bedc7716', NULL),
(560000000000010275, '16899168', '7ec657a4b1b77d670d8820c66512438c45091e3a7ce63d6f386e1df5d09483ec', NULL),
(560000000000010276, '171204jg', '07d107b06d706d702b8f2f56af99364d9afe315afce4bcb5d230bd0b49f0a422', NULL),
(560000000000010277, '181818', '29f5f0cbeea2f41a57dfc489788abfd35e12b4264643ac0d6d298309964ecd8c', NULL),
(560000000000010278, '18881888', '0b4eae97901158b3c874a96f75368ab329dd4d9ab060690c1b9a68c85cfc551e', NULL),
(560000000000010279, '18n28n24a5', '6e595af26be83fd94af753612e5eb72e50f8d7fde8384d5f7024228146e02149', NULL),
(560000000000010280, '19491001', '443a8cccb6bf57f5d48d88c6b619832ef3a4687ae51b3be569c5d2966117e8cb', NULL),
(560000000000010281, '198211', '9a74bdfc7434c84398424d40566cca64e0c7fe66f87d30443fea8d049a71048c', NULL),
(560000000000010282, '19830122', '851bb8297553bdad55b7a590ad587bb82fe6d2c8967e90f472af6e441962e73d', NULL),
(560000000000010283, '198311', '8f4ba561d032e0279f491b006eb86c6ca84a873dd3930a05abef877b47f925d1', NULL),
(560000000000010284, '198312', '5aedbbe58abb6e05efe57a5a93562f01c2e8034bf24f4463b3539d525450dc8a', NULL),
(560000000000010285, '198410', 'cccd186382192bc1a0b3c02c35cfcf831609c2e035f00b95686a2b5562ffd601', NULL),
(560000000000010286, '19841010', '2105bdfdc207fcb90c5f5ad60eaa945672c72a069b5e6d504d86fcc48cacd5d0', NULL),
(560000000000010287, '19841015', '2d542a33f312ecd10c8f6087f906825a3b49c75ffb0d14c7f87109f450890cf7', NULL),
(560000000000010288, '19841016', '497b2411981745217e946df2bad60c8c121815c0bf1c78bc563d5fcfc347f4b4', NULL),
(560000000000010289, '19841018', '6621504609d7e9f6889c7ef5bd73d9d4c82c1bf84022cdb032ceb25859448ddc', NULL),
(560000000000010290, '19841020', '423d0ed7556048e5d8a4f8e564fe0be1393b59f0bbe21f2634c930d7c5bb03eb', NULL),
(560000000000010291, '19841022', 'fe2cee54db8c85e5cc4f878f82cf4f79c23ee71e9e7eb87db195393bcdabb70c', NULL),
(560000000000010292, '19841023', 'f31209333038eceb93f7e63d9f0b5e74b1202d0d406da4971a79f42ac4301bec', NULL),
(560000000000010293, '19841024', '6e4419e5e1777ae37d5dac747d663ad2e4bf1b3affd0f05ae3cc51b822d9e4fa', NULL),
(560000000000010294, '19841025', 'ed5f23afaacf8ef01b4c86f30b3e089be744f3ca4b8e800d5e5a308841b67dff', NULL),
(560000000000010295, '19841026', '81d15692fa5c3ad390d954a2886600032969e82d8ad6a256ec06eaae8367cb95', NULL),
(560000000000010296, '198411', '591a3382dc88413e7ec8632bd8012df00c5f9ba7d5a6039cae9b9a23734b449c', NULL),
(560000000000010297, '198412', '28f35a42f0895d84a16e25149a18aa1dcfdbcf680fb20a17d955696e5ef34936', NULL),
(560000000000010298, '198510', '6cf51f9333f199d0ce3534351e09e327ec155bcf0fd62e8b851cfbfa28252127', NULL),
(560000000000010299, '19851010', 'ba77a12da5989740c4a2269f2054f6c070701b0441271a21a6259444db6f8c90', NULL),
(560000000000010300, '19851015', 'ac9c6e1360cc72ab5cc3846ca0fc0df805ca894028bff5ce54e6789e740901fe', NULL),
(560000000000010301, '19851025', 'b6d38d6a664c49a77aa7d06e505eddf3a16de4acceb1f62904c422ff99f14397', NULL),
(560000000000010302, '198511', 'ead2cb32d1b1a65dcfa4b2dd44c4da65710d61be1c7762576a2946244c64b27c', NULL),
(560000000000010303, '19851120', '52252298f20388a87e6b35b0fff28baf8d6a9c875f758e7927f4735cb1017b9c', NULL),
(560000000000010304, '198512', '3d254666191e76d2fd2b5586c1bc94ade2f455b68adeac315672827511a95955', NULL),
(560000000000010305, '19851212', '36ac2c868b39935a62fe47f1cb4d7cb6179924376d58390509d615c671babe2c', NULL),
(560000000000010306, '19851225', '011ae648144f9e2e4d435472b146cd1fcf26c3d2b75343c23b534e7ed8d4085a', NULL),
(560000000000010307, '198610', '86fa140ee26525c97ba4e7875b4a7f96fd82a88487749bafb0af4bf1ac65875d', NULL),
(560000000000010308, '19861010', '00c6689f434b1099afce9ab331ffc53ed023ade119c2e51ca8b167ad74cf95e8', NULL),
(560000000000010309, '19861011', '8d6d84da1537206461a25a368a92945decff0811227f8ed5be4707c16da549dd', NULL),
(560000000000010310, '19861012', 'ed5ef7c79f5fbe7cea236548fb85a648e37c67c5acf1d25f938dbab05c48e8d9', NULL),
(560000000000010311, '19861013', '50f9ac5e865e7ce769fb92ea3f6d4efad78c5b6fd23965877369c43764fe165e', NULL),
(560000000000010312, '19861015', '7060bb99a7181870f47d6ade21d5e6b45a6c23f351165258947e84ee6ba40265', NULL),
(560000000000010313, '19861016', '8451de95f597e26dcff068a2ecb1180462f17a0fa19c7cdd71476992e3e55c9e', NULL),
(560000000000010314, '19861018', 'b59cd7ef91ce130a79fc63398ab30d7f2ad2db2f1d2f4e97cc5d57471ff2a982', NULL),
(560000000000010315, '19861020', '0b5a73f88e3dca5ec1a102a5169ad847388f48d1424f43743595e76c66cef9ec', NULL),
(560000000000010316, '19861021', '5fadda41265ed5a95c3a454aea632391330b3565098a376d26c38c277304809c', NULL),
(560000000000010317, '19861022', 'dfb7a0e68d4ba303c337d67691d0bb3647ac1b45dd7e104bd7622ba671af551c', NULL),
(560000000000010318, '19861023', 'cad5d53595f3f2cbc0f53f983fbf2532bebd6cbf1df89f20bdc8fc154091d17d', NULL),
(560000000000010319, '19861024', '69857fd97a81d254d50699c6fc3c02d43ef11090ba2978cb8d3a3b16cd697858', NULL),
(560000000000010320, '19861025', '25d62ca78a63f2c82fb0ff52d29ead3d704cca9ee0f86dea5d401692616cc962', NULL),
(560000000000010321, '19861026', 'c298dfc029f965fdbd3b22d75d48d1bac3685baa95c5bf54189a49a4f4f1bfe1', NULL),
(560000000000010322, '19861028', 'c83d878c17ad5592e8d760b433a605383fe2832c3ec277f16ceb2dcb5edd286d', NULL),
(560000000000010323, '198611', '4faef5a5aa609b19ea99c10285a11bd775519bedc081bd6fed155c8bb1bc60b8', NULL),
(560000000000010324, '19861120', '8a971420181dd42e44439f51e1c92131a4bc67795103beeab11c097cfa3193ff', NULL),
(560000000000010325, '19861121', '9600292f033f494caea6a430d8be6ae33c64d284c227d052b75bb34b19bc2237', NULL),
(560000000000010326, '19861123', '9a16cbc2c7182396d3305d75ca59438078d3314bf4b5ed9a20beb407f18e8e50', NULL),
(560000000000010327, '19861125', 'fbf4f4968cea93943d486037b99360443f8b57b224068cd444c649ed60b464be', NULL),
(560000000000010328, '198612', 'd178b6518f6dbf7c9a78ff49b0acff8ab338bac59b086136a7eedecc9f4771f3', NULL),
(560000000000010329, '19861210', '7b4d2795ad32d9aa62ad5d15f674b6ca8deaf7492c031b95bf6812c00378564a', NULL),
(560000000000010330, '19861212', '3ba66e32fec04b42cd7bdcbd8d63b9b1b8e1d9077cdd1e43095a655bd3a58de0', NULL),
(560000000000010331, '19861216', '334437482fd293f4dab6d17c1286c2090e30c348cb12e05c12f1e1c35748688c', NULL),
(560000000000010332, '19861218', '11b853aa668ca00c00a845245426e1bcb9caf61f99da90d1cc848717d41faa63', NULL),
(560000000000010333, '19861220', '8e9d81e686fa37455f5348750379383e0aac0baa21e6b1779b515d60a83242f5', NULL),
(560000000000010334, '19861225', '63d6708e2d70a58d36b294ff44ef879339997d079660fc5c3e47a10950506022', NULL),
(560000000000010335, '19871010', '7870787389214a5e1561031290240755ee7052d04e464abea3aaaf91fee46959', NULL),
(560000000000010336, '19871020', '2ada52d467ea60a4886f419e4f8f57b2f6426a31b917a7766175f964671c2334', NULL),
(560000000000010337, '19871024', 'ad537d6be4e5da7d973cfc40cd826c5e58a6debcce0464c461ecb6a56fcd95e7', NULL),
(560000000000010338, '198711', 'fb14e93d229b8a7f6b6c9aee64190d181432a9104c9878b5547fe4f139db6777', NULL),
(560000000000010339, '198712', '6b7d7b00437c49397dd220c3a2ba10556a81af0365af3a1960d8d106979bcb94', NULL),
(560000000000010340, '19881010', '4f258d42f4e5428830d1db6e2bdf560e99a078297dbb748c5d967744765c71ee', NULL),
(560000000000010341, '1a2b3c', '96b8b43b198b278c2242dd44ed27e80dd3dcd860be69cda1f805ef50e2667760', NULL),
(560000000000010342, '1a2b3c4d', '0a05d7b27cc7a2b1ca704adcbd1d6e3ab2c19ece000586f03bceeabf24547e43', NULL),
(560000000000010343, '1hxboqg2s', '83846194f0791eba5047a1c81924917617f647ba20f8a426a6aeb0182cf670fa', NULL),
(560000000000010344, '1q1q1q', 'f692403b5d76d06f39402944bf0e72eff0f23498d74d27d68c4f8c96710ec60c', NULL),
(560000000000010345, '1q2w3e', 'c0c4a69b17a7955ac230bfc8db4a123eaa956ccf3c0022e68b8d4e2f5b699d1f', NULL),
(560000000000010346, '1q2w3e4r', '72ab994fa2eb426c051ef59cad617750bfe06d7cf6311285ff79c19c32afd236', NULL),
(560000000000010347, '1q2w3e4r5t', '28f0116ef42bf718324946f13d787a1d41274a08335d52ee833d5b577f02a32a', NULL),
(560000000000010348, '1qaz1qaz', '8de8edb14148430ed24a4ffb0b9050acf152f1b85f3e39f7ca54278b110d625f', NULL),
(560000000000010349, '1qaz2wsx', '059a00192592d5444bc0caad7203f98b506332e2cf7abb35d684ea9bf7c18f08', NULL),
(560000000000010350, '1qazxsw2', 'b73846dd535927acb39ffca85c45f41197d0380b1477e3448a041b9b94a222b1', NULL),
(560000000000010351, '20080808', '00ec61a60202cace3029590c352710e11bb1030d62ee1e862daaf9993d1a0af4', NULL),
(560000000000010352, '20082008', 'e5d672f77fc07b959ec15c33f80dbee050458714a10c1922ac19120dcbf093e6', NULL),
(560000000000010353, '2008520085', 'd049711b54f280ac4934d97f4b66c0bc2e80ad21b803993ce920f22441c4ab5a', NULL),
(560000000000010354, '201314', 'c163f4931042e283bcfa528e6bb46c2d0ea2135de2b0fa90517dc832c6fae1ef', NULL),
(560000000000010355, '202020', 'cc3c49ccc51e0bb804a695f67d9f4d29c7f476149d3e79e3455440a5c92f50e7', NULL),
(560000000000010356, '211314', '52bb588ddc390e0dd995855cf95c2c75029410728890a535e8aef70fc4df91f4', NULL),
(560000000000010357, '212121', '55fbbb9de6c380e13013f7f7621cfafdc939fe87c1b0af1cc425aa18f3744dec', NULL),
(560000000000010358, '222222', '4cc8f4d609b717356701c57a03e737e5ac8fe885da8c7163d3de47e01849c635', NULL),
(560000000000010359, '2222222', 'cc2e018aa6eb9612ccd027bbdcdc9b8c8d351789f14cae4d688a876c18938235', NULL),
(560000000000010360, '22222222', '33a7d3da476a32ac237b3f603a1be62fad00299e0d4b5a8db8d913104edec629', NULL),
(560000000000010361, '222333', '7afd036ceb16908bf877aec9b5e49fb9b07525910cf7a8d0f1dfd7c9ec8f970b', NULL),
(560000000000010362, '223344', '19e58efc7f71d3ec0bd46d451e84674f072ccc74c3128f4f017e6981d4e92543', NULL),
(560000000000010363, '225588', '44af9c3ec1c20054fb35ae7d30f8c0b82c8989360d35e60d22d354d204f31ac5', NULL),
(560000000000010364, '232323', 'c81ce2684a7b8d8738cd9a978e5e1acc846eca4b92686420bc1e641d287c4e80', NULL),
(560000000000010365, '234567', '2dc0269fa54d269a87536810ec453cb095b4b92f45e63826a21dff1c2e76f169', NULL),
(560000000000010366, '235689', 'ff98ef67b552532453d1ad8b1912a776ab1b30bf3814fa009b8ffe3c3e5b7efe', NULL),
(560000000000010367, '246810', '7c2523c985881fb2c2b4cfbe917eb12c4c4b61e898ad4e7160cfca487ca3c4f3', NULL),
(560000000000010368, '251314', 'e1e6f681bea650461fa014937dae742b6ec0c1518d1057f056b1a90ef5fd89c8', NULL),
(560000000000010369, '25251325', '0267c67bf90bba89aaac92ae014747cb6d008fbfb118362e5e7db77dba27c016', NULL),
(560000000000010370, '252525', 'd931a74fe3bb28deee7f370e404c97740113e325b75c41335fe7798fbbbb67cc', NULL),
(560000000000010371, '2525775', 'ce68af98782d388b76f273a447051c438073ed093cff72cbdc6f8438c694c30a', NULL),
(560000000000010372, '25257758', '71657baaf0071b47a627453bfb12e2e36f70d412914cd15d16fc16afd6640e1c', NULL),
(560000000000010373, '258258', '460e3f12357f90c0830a12ee13c45e2e886ddad85e3865fd66dc63eb469f8baf', NULL),
(560000000000010374, '2582587758', '3ba76fab867ce8a232606c265d3b5fef75f6f36fea3fca1b2baa2bd1c580e26a', NULL),
(560000000000010375, '258369', 'fd2bb052f4d3faceaa9ef25b04e411bae56b5565acf7934531096ad96f5ec5f8', NULL),
(560000000000010376, '258456', '82f9231a70f4f6cce288625209dcbed869d917d5239cd20b7e2ab8adc3fba6fa', NULL),
(560000000000010377, '2587758', '87dfe571f217a57d70006cca2bd9b1b241c30e552dbf04109070c9db8bfb01f5', NULL),
(560000000000010378, '2597758', '8f3be5902ff35f1a07cecd2abbd7d45cfef3174c3a0e2b02b17c603478fc52ce', NULL),
(560000000000010379, '299792458', 'efbf700e870cb889052cffbe9d66458a3145d88424df9582ae6da734b3fdd3ec', NULL),
(560000000000010380, '3.1415926', '8721d1333b764926ff121fed87539c492af07cc3930258752fc3a26b33a91cd8', NULL),
(560000000000010381, '314159', 'c5b389beb081fe1e43ae92e895deca086b4eed5cf9efc7b78eebbbc9dc75c3f0', NULL),
(560000000000010382, '31415926', '31cc9650f3dd1bca7fdcd1f40a4cd1a77f7a82f0d333be132fec3502ec9d1515', NULL),
(560000000000010383, '3141592653', '0ac59a6eff4c0d73984b7ec775d6a01864e80dbc5e5488c594ed1ae4748ff56d', NULL),
(560000000000010384, '315315', '41016e7c2e8d9dfcdeea9332ae34fdf3e0108e597685aad65841b5a8a314a2b1', NULL),
(560000000000010385, '321123', 'd282c8a31606e750d5e70fdaabe3f17b61eb3df4beec5fc808ba3d854a6c9165', NULL),
(560000000000010386, '321321', '701fd6f18a46f7c72397c91b9cb1a6353744b9cca3aa329af5e5e1124b6b8c5a', NULL),
(560000000000010387, '321321321', '8667c3ec6a3e9bc25b2b8461c8962c8f788458ccb24676449a884a7f9b7a9580', NULL),
(560000000000010388, '321456', '3af80ab0454995c52ebb1f4704fff7b71e61e39f2dc428d9fdef762b669ec811', NULL),
(560000000000010389, '321654', 'e519d416c8b2623331c17695e7c6ab641e96cd0ce3f636b097b1be68fd793e16', NULL),
(560000000000010390, '321654987', '3a5d9797c6b9313b4e79eed8c8d369e94eabe3700cf864c625e510ebba02f5b7', NULL),
(560000000000010391, '323232', '496645fd7fc9302bc9955b4439722cdfd81a20b5eff797e5392e243f9cc86184', NULL),
(560000000000010392, '332211', '938521e0c82d69844e9024d3b71a59ecc7b4313160dea1e75287bf0b892f8446', NULL),
(560000000000010393, '333333', '68487dc295052aa79c530e283ce698b8c6bb1b42ff0944252e1910dbecdc5425', NULL),
(560000000000010394, '33333333', 'afb47e00531153e93808589e43d02c11f6398c5bc877f7924cebca8211c8dd18', NULL),
(560000000000010395, '333666', '9b4bccf5bb65855dab64a26d75b8d7eaa025cc8a590fa8f88e7b2aff81095530', NULL),
(560000000000010396, '3344520', 'c6deecf7c04af50409ae348e4253e148f9bb447c5dbf585eaa1e903029d5db51', NULL),
(560000000000010397, '3344521', '6fa97ddcacc3a8f83270ad63e1c571afb5b27bfabdf885503255daff64651d2b', NULL),
(560000000000010398, '336699', '1af60cb6cd7d342d86723a84e196b417247ecc50cb890a90bc797a1849428c86', NULL),
(560000000000010399, '34416912', 'c26a5aa0a3b24b210b7c993af8017aad24e35862c7090e3a6c0e12788fa9a572', NULL),
(560000000000010400, '369258', '6e3da59bb00a82c3222d8dbeb62c5e0922c2ce4d9aff5b98e3f0bbaa6aba32ae', NULL),
(560000000000010401, '369258147', '258f264e2ca3f2376ea542aa491ba071b24bc04427f45610d7b56d83e9f347e4', NULL),
(560000000000010402, '369369', '0ac40d976122b3fee3d9319ca58d77586ef4252394a443426f815b73ae5ec9bd', NULL),
(560000000000010403, '369369369', '558b328f372732169ff4c0d5dc770eed901523e4e053a89f18ff3b051ea0dc27', NULL),
(560000000000010404, '369852', '68d31642d5ee64f81c7706f66f82a8041540bd682d0318634a45614385a7a322', NULL),
(560000000000010405, '369963', '7c4b1aaa6cc5918c40c5dc128d2d17213697296de52506642be331eaf8cd3333', NULL),
(560000000000010406, '3838438', '2f775b91b1505ed9bf2c70792df6095b9f81b042c4bb9c5cd9fb69a77d2bdaf8', NULL),
(560000000000010407, '444444', '69f7f7a7f8bca9970fa6f9c0b8dad06901d3ef23fd599d3213aa5eee5621c3e3', NULL),
(560000000000010408, '44444444', 'b3c4b40750a97212e8981e4ac494d1ec77053f1eaf4e0934c276b74fc4f87c48', NULL),
(560000000000010409, '445566', '48e6f958531e543731746fd0a4fcba173e2ae226d60eb19a5d021be3c29f7a3e', NULL),
(560000000000010410, '456123', 'c1cf024576e9c756b252bd5035efc64c72c17affe236909ded190d266a5bfdf1', NULL),
(560000000000010411, '456258', '8364bda8231dd76a872334cab7e42f3f09285cd10d0f27395903d1fe08b6b6c1', NULL),
(560000000000010412, '456321', 'ad4ccd1cce20ff71773bffa3034858f8aff1033e4fd7e5660a0fa4d32fe29a44', NULL),
(560000000000010413, '456456', '54bb6a0d2ea7d49744e886aa20859d70b6fc4ee0b9f144353ecb4b39195767f3', NULL),
(560000000000010414, '456456456', '4cf8c576d4914a2a2a58cf230c63d0a84b91c1bcb6dec0c95377550b76965844', NULL),
(560000000000010415, '456654', '137c7b2a050dff7a49ecb0626ec0f948aa24bd9fb365f773d92272aefd528c81', NULL),
(560000000000010416, '456789', '472bbe83616e93d3c09a79103ae47d8f71e3d35a966d6e8b22f743218d04171d', NULL),
(560000000000010417, '456789123', 'ae74e53a6f447e10150dd2e83ad3f0289606aec5354ae31fe87f3500b802dfd2', NULL),
(560000000000010418, '456852', 'c0d332f416b8f1acd4968a2594d2c2bb5d4545cbb43fb403445d7924c670d3ed', NULL),
(560000000000010419, '476730751', 'e7fb8ec398525e1dd1caef1e8eec978ab43c3b3d9ea0dfc26dfdcf7eef5ae4b9', NULL),
(560000000000010420, '51201314', 'd17e48f06e6d29c5c7c411338b858790893667c6869042efb7988e1d87e34254', NULL),
(560000000000010421, '5121314', '32e4b3cae1b0c067dba007e8460f88591d06f172c9b838594a0f8a46696768f2', NULL),
(560000000000010422, '518518', '86623cafb592ab3769a9e5bf6ee13e80e61550f618455ec9cf4014c47a2996dc', NULL),
(560000000000010423, '520025', '90dc050ed4bb024b5528bdf2f6571872aaca8ef41dcc3c39d9f0cea957eb517c', NULL),
(560000000000010424, '520123', 'e6f1d873007c8949e14475fad54bd9a92b73a86bb01ab1d2555066a4c0637974', NULL),
(560000000000010425, '520131', 'd98f033606a948d88436d6fc23ba6eb52723cc1b816cd3e69360b8854cd9afa2', NULL),
(560000000000010426, '5201314', 'c04d6e34aab689c5c0e68eb51753c843e032efa7c16427f8642ee07ab946e981', NULL),
(560000000000010427, '5201314.', 'abb97ef46b9756505c71664698bc2ff316e4b076aa11f307ce908d00f0cd1660', NULL),
(560000000000010428, '52013140', 'd00f909e328abaf82c0ea34f7580c4591f98930a55e7df9617ee9c54f7661ec2', NULL),
(560000000000010429, '520131400', '5a568105ca4041538b639842243b2e5015ef3f2283c4f2aee1e16eeb6e41d16c', NULL),
(560000000000010430, '5201314123', '01890448e6993479a789d7b083e5e31555e0aa11312ebfcadcc3c6b7d68fc255', NULL),
(560000000000010431, '52013141314', '8f76f2c51da37c73106116593d1d0c363ae15a2c29242d74b9d31d40a547e363', NULL),
(560000000000010432, '52013143344', '9aaa0906a33d66ec47a986addad4e1f22c7ac7af7808bb36ad39653851041c6c', NULL),
(560000000000010433, '5201314520', 'c6ddc573a996d8e35bca9f53f689a1c2de690da23e3914f30ce01b6fccbc9dbc', NULL),
(560000000000010434, '52013145201314', '07135dc1aab1561227643d9d1fe6f919cc576cb7d757dc2994bfe7b23a7c72db', NULL),
(560000000000010435, '5201314789', '7354f81df7dc20d4ea55109573e0c04f81c19b08db249642eb93a3a3ee04d942', NULL),
(560000000000010436, '5201314a', '466c7529a32212554bc1ccd21cd9811df33fc4dc05685ab1e15201a64c3edcd1', NULL),
(560000000000010437, '5201314q', '8c67b02b02e3b87f9cd1ef787c19a718b7924894cceefa19d8cadb1338632c28', NULL),
(560000000000010438, '5201314qq', '7e243c0b995319419f296c240fb2a2229a7324f53fa4fefdb3457cf62217986f', NULL),
(560000000000010439, '5203344', '2f18c5fbae51168f1926f6ec29a04fe363d49d1e2bee35c13bd712c1ddb7fdbd', NULL),
(560000000000010440, '520520', '7182a571ddbe4752823cf4b8c38fd98720ae3ffac2aea0c22dd462fa8f6f0d9c', NULL),
(560000000000010441, '5205201314', 'd37657019d6a034c7c5091b8538d661f3297c3ecf8f0cab491eb9f21d1362d37', NULL),
(560000000000010442, '520520520', 'ab4f00797fc349d1b880f671da5b9b73970e8207aaf8f3b53f5ab47c37fba2e6', NULL),
(560000000000010443, '520521', '163fcac83f389a8030d7d525deff234d46f72087785766be3edb6fbce2d672ba', NULL),
(560000000000010444, '520530', '0265a4b4b1987adcb25fb840bfc93e285fca9be729114d4c10914d10ee6b8a98', NULL),
(560000000000010445, '521000', '5cce2530ad00ebac55552532f5b2142ea002fa063f976a7337b707553d7534e0', NULL),
(560000000000010446, '521125', '1aed769e32c4d6941e541a2f0022c661acee9bbd365717ddc3bccb99a48c2303', NULL),
(560000000000010447, '5211314', 'bae6b538c1102df1b4432f12e49933ee49c279c55b58fe079d3abaa384750b84', NULL),
(560000000000010448, '5211314521', '921da6996caab72c4a7a8ec474d7338c94a3f9317e59ae341d6c2ca4ff06fd45', NULL),
(560000000000010449, '521314', '0ee89ff4e68784cca9b7d12de1afca5551a0829c7f2b005480a47cc97943778c', NULL),
(560000000000010450, '5213344', '1db1ea10d732d1b00357d0d79e1627388d7fd99b2c5b4f8226380083386b56d5', NULL),
(560000000000010451, '521521', 'b83d949e27131d1d054e5267efb05c7883ad0b046bc6f56277dd5f7017fad706', NULL),
(560000000000010452, '521521521', 'ff7550cad651cb9f6bd804ab68d6894fb501378382a31698d4f2ee1bb7af4dd0', NULL),
(560000000000010453, '52tiance', 'd2a329d0d4f18e3e86c5dfd90ac6ae674e0bb38e1fcff252558ae3c6e197f684', NULL),
(560000000000010454, '54545454', '52a493a505500a5486c1c1b9c7a228cf67771f0e8bfa5a306963961f3dece30c', NULL),
(560000000000010455, '5508386', '3888feb6c2b212438dce6b1fb0dad6e35efd205e55a5bbac701224bb22777bb2', NULL),
(560000000000010456, '55555', 'c507a68f3093e885765257ed3f176c757aaf62bb4cbc2ef94b2e7da3406d9676', NULL),
(560000000000010457, '555555', 'af41e68e1309fa29a5044cbdc36b90a3821d8807e68c7675a6c495112bc8a55f', NULL),
(560000000000010458, '5555555', '8783b7dff7a7109035e8df613232d21b3f4a4aa532fb8f53d1059046814dc657', NULL),
(560000000000010459, '55555555', '01c02776d7290e999c60af8413927df1d389690aab8cac12503066cf62e899f6', NULL),
(560000000000010460, '555666', '0689211a62ca8488b19cf30dedf9059089477c004924026bf7e1095f49a21b80', NULL),
(560000000000010461, '555888', '146cc3f506268341f314c63b142b26534d09e6ec925a9f22f3214d65bbbf4e88', NULL),
(560000000000010462, '556677', '270fa1445d2cd102ce2ab33bc7e1f03a5a63beabce213c1f495a00ef11e1c5f5', NULL),
(560000000000010463, '556688', 'a35959c7441a27f4329f000ecd4fce23347b8daa4ef66554ddce31371e90e250', NULL),
(560000000000010464, '564335', '451913470f6d663ac4832b48d3e77933315a74260695ed6b3cda2d4005d70500', NULL),
(560000000000010465, '565656', '4627e00ddc49e55b22ba8420be6dd0b94607bec7c4ccdd9d20e37e252fefa776', NULL),
(560000000000010466, '584131420', 'd7ff9b2e395f71f114f05a5d4fca997f4f9414614e25eb50edb6e6909fa62f52', NULL),
(560000000000010467, '584131421', 'f4eee833356ac4f1c7f653025e20989f791c97b1c6aa215dea20f6ae5f401968', NULL),
(560000000000010468, '5841314520', '4471ba67d08f1db63496770c3eaa862bc49d2a7eec7bb03712eb4e5af2ab8a4c', NULL),
(560000000000010469, '5841314521', '7fffa8dab6537c6eea8f8692e081d41f91cd1b0e2194c16ff4ce41ff47991689', NULL),
(560000000000010470, '584201314', 'd6ba41bc7cd475e7efb5cb926ee5a8f38d13d3bd8a5732ed3d36ab709bda7b68', NULL),
(560000000000010471, '584211314', '5a89902be4946669ae25905bf0285ab9d8ae4c0dcdad6877e76af820102fedef', NULL),
(560000000000010472, '584520', 'e82c4abff966a93c62b070d641b3dc898ef0af947d5fe5b2c4de45e543e5c8d8', NULL),
(560000000000010473, '5845201314', 'ba350231d146ea7e727c23561f12aa22100d4086d72df11fbf9f21e555969451', NULL),
(560000000000010474, '584521', '2486f72866ed66c821b62329f0360d4f6790fd258b43c9d3043316e7172e7357', NULL),
(560000000000010475, '5845211314', '32754a18616ff756c3d20b9500f20abc662256eaf9825665d1274cf083d69854', NULL),
(560000000000010476, '585858', 'c8206bdbdb7666ed3d86532bb7700f5b1574fa0e71794d4e15eb497d833bfbd7', NULL),
(560000000000010477, '623623623', '1b94ebe975976a27670ff4005fa6189fc00f0cb08d297e58e201b451726224d8', NULL),
(560000000000010478, '635241', '0358f6aa59788a6deebad09b3ab91f6f7dcd17363c621064be75e7a8bc588a74', NULL),
(560000000000010479, '654123', '1faaad5d381a89f375cce6a9e0d659c1100b3ffcabf5d9c4205be97e9bfbd654', NULL),
(560000000000010480, '654321', '481f6cc0511143ccdd7e2d1b1b94faf0a700a8b49cd13922a70b5ae28acaa8c5', NULL),
(560000000000010481, '6543210', 'd80a33333f2b696325762a3478b0497b8dc08edb8e3d56848aa0f8f2cd439826', NULL),
(560000000000010482, '665544', '83e7be4417335469795cc13d3af3eef9ce63c3f62595492863668e474d449ec2', NULL),
(560000000000010483, '666666', '94edf28c6d6da38fd35d7ad53e485307f89fbeaf120485c8d17a43f323deee71', NULL),
(560000000000010484, '66666666', 'cf1aa821ddabafa6a16212eba3805fbcff92c6fc981d43967e1fca8657f8571c', NULL),
(560000000000010485, '666888', '6d19ca72de1fe7973e5a763cdbaf47af34674d4f45a8a2cacd881b751d680a4f', NULL),
(560000000000010486, '666999', '007ac4200b49a365708b5c1032ebda275ceb56a5c1114751da2c1ffd707ed689', NULL),
(560000000000010487, '667788', '43cb39dedd55125e05517707609603ee95b076a56c110c6c5a5b00dc0d7a557d', NULL),
(560000000000010488, '668899', '237c85a8a6dbdd531694413ac7750d302de1862db17d80d519e5c92752f37445', NULL),
(560000000000010489, '686868', 'f0161ee053b9d46b5d926b093c8d54e86816be9e1be28f34a9f152de6491df30', NULL),
(560000000000010490, '696969', 'c2eb7898bb6771503ffee5d0c722e5b561fe480edbc30141880a1cdf1e5b1cf6', NULL),
(560000000000010491, '7007', 'afdcba2e170f8107dd68c4227dbbbf0bc44c7b9ae55506ca331c5c168840ee80', NULL),
(560000000000010492, '709394', '457d5edd060f6ec63ade4b372191a29b014f20b1bc5791e304eb9eb822f29ca3', NULL),
(560000000000010493, '721521', '70d40f17709dfccf56919eae4463c95d6da0178709fdf7b70c194d3fb8bb557f', NULL),
(560000000000010494, '7215217758991', '72e385f819dfd0460622c177c3c9dc34e341dd7064da843f4458e36b9460a85c', NULL),
(560000000000010495, '74107410', 'f9e3d192f5d2a7b4bbbfed0810bae3cb0e1b1eb4eb81ec525d8ab232c28b779f', NULL),
(560000000000010496, '74108520', '1630d24fd3db4fb4f2a286d33120320d84c93c3d45a6eb088cdda3b1f6d06103', NULL),
(560000000000010497, '741741', '6ce5d724d32bdd2d72867fc3c7c752ec386ef8a3db6e73c7c3932d41f0519987', NULL),
(560000000000010498, '741852', '0e06a93f7888d926e3f96ca2d5607e220ed183f20b5332e885a0bfae947d2241', NULL),
(560000000000010499, '741852963', 'a5b810a3190a39033de4d82052fdf6f4c9765516d6b7eeb0c496adf8a3d3efc9', NULL),
(560000000000010500, '7418529630', 'bc4a7d855acfff803703a68603e065c4356cb53767a207f23df96077f2d37895', NULL),
(560000000000010501, '753159', '594cd78daf32565f1f17d3aaa6455f0a19a110496e8050655b4b23c4931bc23f', NULL),
(560000000000010502, '753951', 'b2133cc3beef2779a324891c7cf4ba62f26f7108093cb2d3394ab1b0998f4ae3', NULL),
(560000000000010503, '7654321', 'fe68a21fc76bba7b3a3d8e454eca8cd258de68fd08dddf035f23ddbdce6fc049', NULL),
(560000000000010504, '770880', '84eee289aaed0156541b079cd0cff292211a21339796f47c58e17f2c93d3b07a', NULL),
(560000000000010505, '7708801314520', 'cd7c5eb6d25c38602f7ee7029c335f714c7344e96d37928884a33daecff20079', NULL),
(560000000000010506, '7758258', '4bcce395ba3abe073548fa9c20765cfda27bc82600eddede5393dace61249f45', NULL),
(560000000000010507, '7758258520', '252f5c92daff9f2a48e821f5681e8f71bdf16f4a7dbbf35d95affc164064fa65', NULL),
(560000000000010508, '7758520', '92464857618ca11f881f955ab73214abeea4ce2a3b445abbe2d57eae5a45ffdf', NULL),
(560000000000010509, '7758521', '5e7d5a0361c8863df42a52b75b329ecd81a5014208250a468946047df488b672', NULL),
(560000000000010510, '77585210', '3935b74b12bf607f4d6cd1f4c9ef6985e9d96de090ad835aed745b4b826194dd', NULL),
(560000000000010511, '775852100', '3041bcc9fce0bec123b8cc8b67169de2c9cf9538b67d3754c7938cabddfc178b', NULL),
(560000000000010512, '7758521123', '02f52e3332f80ff005eb05fb5a51271180b412c67fbf5d8a8b05247585a7e798', NULL),
(560000000000010513, '77585211314', '20700214b4d38cd3231309263e6a35c4042e9f1bbfe8bd79c9c4de248f031ebf', NULL),
(560000000000010514, '7758521521', '88d369925959f9b84348b2f056a8f2de8d3ae9369a9afdd37f59e27a934838b3', NULL),
(560000000000010515, '77585217758521', 'f2949fddfc95635febc3513b42b7b7cc97f924866abb1868da0069e9a969dc0f', NULL),
(560000000000010516, '7758521a', '8121acde424b99a3611ce746349fe37f0430c26ce12ee66c4a58fb52090e3ab6', NULL),
(560000000000010517, '7758991', 'a8d8c81cb862ab6b096790fc41a2cbba4775899a9d1bf329384b8ba4a65dcfdb', NULL),
(560000000000010518, '777777', 'ec4c88ca7f69534f10c0611c1ecd13e7c2cdf73e1b915e9fd0cf27ac10da43fa', NULL),
(560000000000010519, '7777777', '8c1cdb9cb4dbac6dbb6ebd118ec8f9523d22e4e4cb8cc9df5f7e1e499bba3c10', NULL),
(560000000000010520, '77777777', 'b870d3e3827088d978fbc2606395548df80ab0027fbfbf806a300b7b4f9bbe01', NULL),
(560000000000010521, '777888', '6ccf5eb0b98684778c3b1a5415fdeecd6819dd2ef1cfb22eee2c775cc41dc9cf', NULL),
(560000000000010522, '7788250', 'f9bcc95cf3847e0766538c6f41a1cb3558e1407d7cf01eea83535919f5f299d1', NULL),
(560000000000010523, '7788414', '42474edf8e334bd0d36bf51f7ec2f03343529aa7ecde7ee47d50ec876e6739f5', NULL),
(560000000000010524, '7788520', 'ab0e7e8f96ce52b9aafafe3611bcae551ead0024c50ef352def59b601295527e', NULL),
(560000000000010525, '7788521', '7ab64811c59056104c249a0e3ab453983350268079d9658654ab267c0f160430', NULL),
(560000000000010526, '778899', '29624e2e4c4ccee26ed8f3e0ca1012ea57a8f2191be6149f632250f7036119cc', NULL),
(560000000000010527, '787878', '1a9df7c539541cdd98af1483fe5926ab868e2cbe9cba866b9d859ab9fc93723c', NULL),
(560000000000010528, '789123', 'c0034605ea413370d5ad022b8d2f7fe33461bf6d7e5f4ac78f02c27b793673c9', NULL),
(560000000000010529, '789456', 'e54fc6b51915e222ba6196747a19ebb8dfa651fd2b46a385a0ded647fbfefda0', NULL),
(560000000000010530, '789456123', 'ad4941386c090ac54142d38b390d313075deff4d873a1c82e3a25540cf611127', NULL),
(560000000000010531, '7894561230', 'f6dbd3d75dee4eb50c556e2a30446c0e806cca161acfeba708b5e6fed8ad633a', NULL),
(560000000000010532, '7895123', '8beedb9068239aa2e47b1d31c551e5cd5ce5fb1daad51479a136a2af677e06ed', NULL),
(560000000000010533, '789632145', '7c8c60a19fa0b6df27b2aa0cf6af9d84dbe037b2d92070481d72a4f71b36e24c', NULL),
(560000000000010534, '789654', '91e58dcb509f745680c619477d811449ba9fe9a55ee7a65cacbc685288fbd555', NULL),
(560000000000010535, '789654123', '27e207653fbc09a0c4efa7671e3e6ed02ec74dde0990485c221dd4f1f2bbd0ca', NULL),
(560000000000010536, '789789', '6b8ae3329753d46a4af298ff5b30508b0697a279de761189985bfc7b879e25f2', NULL),
(560000000000010537, '789789789', '4c02c368e0e50e79cf421e2249aea1e8c62ca24c805d61bf58df5ff93489c0d0', NULL),
(560000000000010538, '789987', '573018e4d8bf6e21a2d40e1b602f5a44309c4b918c02185bcd42f1e08f0041c8', NULL),
(560000000000010539, '8008208820', '807ecf7f4eddfbce67993e77f11deab7f818384d3bdc4f76a813c8fcef221512', NULL),
(560000000000010540, '808080', '585814e2bf60bac12ef3450b375ab1726021d01ad1d7ebc775e99106f3db7b32', NULL),
(560000000000010541, '811009', '513251b6844ec8d34fe34b9c8b2db5c97c1d2edca40d03babed1abe84dfa04d5', NULL),
(560000000000010542, '831213', 'aaf474b5640a651fd2091d8fb3190a45d3c3672ae9d0ba1f33b72d8f73b4b50a', NULL),
(560000000000010543, '85208520', '5b42741fa51b0d8e42ca6a3495180f7fcc2e7fcf1cb081815faff52823943111', NULL),
(560000000000010544, '852456', '1602b4048b35882a346c6887360d0343cde72797314f81a36065c71320562615', NULL),
(560000000000010545, '868686', '91ed00d8f12aae1bb39c23ce2c351d5da546fcad58cc2738e6f0ef5a7a963395', NULL),
(560000000000010546, '87654321', 'e24df920078c3dd4e7e8d2442f00e5c9ab2a231bb3918d65cc50906e49ecaef4', NULL),
(560000000000010547, '888168', 'ab8210d20937f315090a1790bde035482bbad5b61df625e75f2b08a498302b17', NULL),
(560000000000010548, '888666', 'b49ca55e12bc80d8735d8924cad613d7fc62e6c9ca19bcec68f4be55df2a5e2d', NULL),
(560000000000010549, '888888', '92925488b28ab12584ac8fcaa8a27a0f497b2c62940c8f4fbc8ef19ebc87c43e', NULL),
(560000000000010550, '8888888', '13e4a576d4c4db42c71f3d6a30fb6f58c6d08cc54bc72e37c67db34721d8ab75', NULL),
(560000000000010551, '88888888', '615ed7fb1504b0c724a296d7a69e6c7b2f9ea2c57c1d8206c5afdf392ebdfd25', NULL),
(560000000000010552, '888888888', '68bd1464f79367d0530965ec2f2e97be9845b19d027f759634fed555030a10e3', NULL),
(560000000000010553, '8888888888', 'ffec2b04828a94f08d28b1d93bd9702f0e3395930cc3f3593d1182beedc23561', NULL),
(560000000000010554, '888999', 'd619e89bdaae0de8760ea721fa1ba8d9a819870b1ba82d720e7ac802270fce92', NULL),
(560000000000010555, '898989', '8fd5b48f3822dd9b7626f16b411dbc3aec05fd594db0fd9d603d4fdb8a65c173', NULL),
(560000000000010556, '911911', '7a7f04c0aa384e6ed4890273c07683c942e373f23e78e39e9418f9792e9a798d', NULL),
(560000000000010557, '951753', '914ec42988d42410e36eb4bbf067d2e57d24c5726bcd64127fa1504bc27ccc0d', NULL),
(560000000000010558, '962464', 'b1a2ae071a2b2aac0dccb45b70cfea54fb1df4c592973b03731ce99c35b6bc77', NULL),
(560000000000010559, '963258', '6b2a33f4d7ccddc176fdc65a2e6d9fdf39f161e5afeca296c50c3eea94d40924', NULL),
(560000000000010560, '963852', 'af3b7a26bddd5de85f59064ba3533a078aef0c8402b6e5533fd70b9c5820b0eb', NULL),
(560000000000010561, '963852741', '66befeedf2d742a28ff44606f05c583271ebb743d18fdd34c0efd831f7f8b9ec', NULL),
(560000000000010562, '9638527410', '4a6979f34efc8fa1f4899da7b13bec33719cd5d89efbc7a9c3b601a601db9fd1', NULL),
(560000000000010563, '963963', '3c54eea70ebabbfb577d27a968ba8ce41d8616519319cc1ee1050e1dc125a3c4', NULL),
(560000000000010564, '980099', 'ddaf87f8786374ca27b716bcd596614e82a0af5804aeaa725e979064f48c47dc', NULL),
(560000000000010565, '987456', '71f780a111c4a0c1e744c31a1ae05299a3bd32467dd60caddf4a961ff793d181', NULL),
(560000000000010566, '987456321', '69da06e38c3cbf801682db81226a1847f536c632c2fbb2e2ba21ec256206f7dc', NULL),
(560000000000010567, '987654', '2a8610aefdd0028c6bf074dd18721c0ef8bc43241cc7a653d7aedf2036bdf6b3', NULL),
(560000000000010568, '987654321', '8a9bcf1e51e812d0af8465a8dbcc9f741064bf0af3b3d08e6b0246437c19f7fb', NULL),
(560000000000010569, '9876543210', '7619ee8cea49187f309616e30ecf54be072259b43760f1f550a644945d5572f2', NULL),
(560000000000010570, '987987', '4c66d7d593d3a8f1ca6c32a8124b7a6ed61d070212a79256338590fc43c4751d', NULL),
(560000000000010571, '989898', '106341ed6691d80fa296bb3cee87190c154e6579e048716d28ab5e6a336814ae', NULL),
(560000000000010572, '9958123', '9cd809d5a80a06df52c52786285f2bb75a12cf5609cb3284407d3eb812ea0b47', NULL),
(560000000000010573, '998877', 'c7e9988d0ac79f466bf33b7bc5a8e31ebad2fe1f6e17c78919f5717acce44230', NULL),
(560000000000010574, '999888', '685f188e4f25af63603dc5b579b31090f459381a242bf7002c8a8e8ea322a4ef', NULL),
(560000000000010575, '999999', '937377f056160fc4b15e0b770c67136a5f03c15205b4d3bf918268fefa2c6d0a', NULL),
(560000000000010576, '99999999', '3f08d8fadb4b67fb056623565edbbc2c788091d78fd24cbc473fce3043ce3473', NULL),
(560000000000010577, '999999999', 'bb421fa35db885ce507b0ef5c3f23cb09c62eb378fae3641c165bdf4c0272949', NULL),
(560000000000010578, 'a00000', 'b3df287bfaa7f1ce9e3a29ccda6787261529c76dcfa0a3884d41f1df785b84b2', NULL),
(560000000000010579, 'a000000', '0cf1ef7f25542152d75871f1a207e1129826886ce5d47ad7265f53dac0f943f7', NULL),
(560000000000010580, 'a11111', '0fb6c6c0b7621fb7bd6ff1e6fb656bc746e2254a4f671dee25c0ce3ddd9ccf3e', NULL),
(560000000000010581, 'a111111', '0ffb3fbc9dfb49dc93e84845b238616298dc42adc72d417e8174af6765fd76f1', NULL),
(560000000000010582, 'a1111111', 'd2e0a90b46d1163632ee571b3ccf1b674ba2c30e4b690159cc5f94e51c68fa50', NULL),
(560000000000010583, 'a11111111', 'a60f2f5a3d320dc2dbf00b471fa4df74dc21594bb891ed3587cddea7fab64d0c', NULL),
(560000000000010584, 'a123123', '84c45e72ebff7042e000b2bfd67b22367b18ddcb723ce4c32f9f1b2064999e08', NULL),
(560000000000010585, 'a123123123', '768d919430d44eb33eb9eca2c0497e5442f4190350e295cde64bfd2f297cd0a7', NULL),
(560000000000010586, 'a123321', 'a5337d17b6c8a0fd9dd87af8fb746a6bc1b792e39b47da5086fc3cf67942525a', NULL),
(560000000000010587, 'a12345', '62080f96a2bfc48794326c5b9750942d15886e6a9746fc215cd0d04127196db2', NULL),
(560000000000010588, 'a123456', '20f645c703944a0027acf6fad92ec465247842450605c5406b50676ff0dcd5ea', NULL),
(560000000000010589, 'a1234567', '9626c7444717aab7a3bbdd509bcafa35a7491e9478d421b38e539a621f695edd', NULL),
(560000000000010590, 'a12345678', '9fcefc0080d894e83ca7d360ce5ccd9ead2c5d8a80a10f9fa9698510aaba865a', NULL),
(560000000000010591, 'a123456789', '5f1059ff008c294b854f44e80bf29af7794725e7f798f92a30e82d80c4f0cf62', NULL),
(560000000000010592, 'a123456a', 'fa4d5ebf3a48c9ad609152b9d10305939a9ba6865d568ff774ae48b8398635b9', NULL),
(560000000000010593, 'a12369', 'd24f7862bdf408b1601bd7a26af7a70de61178cdb0ab68ed3c35307753132b9a', NULL),
(560000000000010594, 'a1314520', '45f708c6e1e708a298466fa402ade8e74b7afe28aab73943b91ac3b4963434d3', NULL),
(560000000000010595, 'a147258', 'a375b573f3f557de363871f3aa296a58ab038ef7c125ce8679e886806af4163e', NULL),
(560000000000010596, 'a147258369', '565cecbc4b8216c1e096e2502b753c1147a46604c913da8d5b64e81f6538f7ae', NULL),
(560000000000010597, 'a1b2c3', '4f32044a655f32e8528edea64dbfd11cba810b8790e6e6e23d28ad3a75980734', NULL),
(560000000000010598, 'a1b2c3d4', '7dcf407fa84a0e0519c7991154c4148de0244d7589020c0d9842db9efad82094', NULL),
(560000000000010599, 'a321654', 'f00a1a65ce7a52bd4e26c291c4f7dc9450f63e74127ec3a552c378084e51e3d4', NULL),
(560000000000010600, 'a5201314', '303c44a338b77a649a9f5859fddd76252a9ca7ca3c24add83eb20bb79fe9f59b', NULL),
(560000000000010601, 'a520520', '6f60c24e56446464a344c47f8e22e97ada980362af50be2b8a57c4175927503d', NULL),
(560000000000010602, 'a7758258', '5c12640ff8ab113a978e9b10f9ff6e30c54ea25143339475b23d3aa7e839c28b', NULL),
(560000000000010603, 'a7758521', 'e74096d203cc3c0cd9eca2f97c1937d62ae2a3e03fe05fc2e71981ba368fd78f', NULL),
(560000000000010604, 'aa123123', '74c31193ed3501e4ebb08942b8b42ac6883a1a4b032f98589661688025793fbc', NULL),
(560000000000010605, 'aa123456', '094dacfa4ae26448b7e7fdb6bf45b639ea9c9de2f942aa42310305f0657f9c61', NULL),
(560000000000010606, 'aa123456789', 'eee87cc39bb1a8c83082866237cbd0e9a40ca003c75d555efffbb937502b4d06', NULL),
(560000000000010607, 'aa5201314', 'a316de0f7e55aa84888e79966d5bb8a15df3bb4a18a38a656e418bfab0f96a1d', NULL),
(560000000000010608, 'aaa111', '4f56fe65c8bd5296ca6a5f95faa0d65fb54b1ad8a87a1f816c7206803bcff938', NULL),
(560000000000010609, 'aaa123', 'f64561e04c3be9cea6271afcd2b324f4b8654ed1b011f4f15ff4436e0100d5a0', NULL),
(560000000000010610, 'aaa123456', '555d329f50d08e06b2f9d324856bfb2ef7b3d69d7c1d2730d5a2fbf05c3acb5f', NULL),
(560000000000010611, 'aaaa1111', '754068f93ca0903e1db7f0ad3ec5a616179c738f462959dd2380b6e2743680db', NULL),
(560000000000010612, 'aaaaaa', 'ed02457b5c41d964dbd2f2a609d63fe1bb7528dbe55e1abf5b52c249cd735797', NULL),
(560000000000010613, 'AAAAAAa', 'e46240714b5db3a23eee60479a623efba4d633d27fe4f03c904b9e219a7fbe60', NULL),
(560000000000010614, 'aaaaaaaa', '1f3ce40415a2081fa3eee75fc39fff8e56c22270d1a978a7249b592dcebd20b4', NULL),
(560000000000010615, 'ab123456', '595a92a9ef887d8f780cb5d77f1a863c3cadad1e1bad06e77adeb3dad8b8e809', NULL),
(560000000000010616, 'abc123', '6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090', NULL),
(560000000000010617, 'abc12345', '14f8f4bb8c0e79a02670a5fea5682da717a5b3d3dc7b1706f7a4bab9afae18c2', NULL),
(560000000000010618, 'abc123456', 'a03c32fcd351cba2d9738622b083bed022ef07793bd92b59faea0207653f371d', NULL),
(560000000000010619, 'abc123456789', 'c6b3e5102f268d17a60562720abeb625b0d3398289f46f51861f1bab3055e89d', NULL),
(560000000000010620, 'abcd123', '983487d9c4b7451b0e7d282114470d3a0ad50dc5e554971a4d1cda04acde670b', NULL),
(560000000000010621, 'abcd1234', 'e9cee71ab932fde863338d08be4de9dfe39ea049bdafb342ce659ec5450b69ae', NULL),
(560000000000010622, 'abcd12345', 'fa1de4364cfd94d75e7bda5d0583bcb136d6437c88a36dc06bcd64566a3530ae', NULL),
(560000000000010623, 'abcd123456', '5fae31539e070a690c1b63720c25eb5b86084b5098a942c86c89c1d67157ed6b', NULL),
(560000000000010624, 'abcd123456789', 'db47a50eb235706cee59de5d73ff4d4722fa5abd0bc8ec7722d957100b47bca0', NULL),
(560000000000010625, 'abcde12345', 'a7411a3704a56d0f9319ab779f26e6b14ab739435ecfa99f4b7c8dafb649b7d8', NULL),
(560000000000010626, 'abcdef', 'bef57ec7f53a6d40beb640a780a639c83bc29ac8a9816f1fc6c5c6dcd93c4721', NULL),
(560000000000010627, 'abcdefg', '7d1a54127b222502f5b79b5fb0803061152a44f92b37e23c6527baf665d4da9a', NULL),
(560000000000010628, 'ABCDEFG123', '200f5183a8d9ef5339eaf6e3987d892e8751036beaa158257c1b65d78e3fa0f2', NULL),
(560000000000010629, 'AI123456', '361f8ad16ab51e69b0bd47c6fd7bdc49e17fc05205167dfe03ed0487843ac221', NULL),
(560000000000010630, 'aidejiushini', 'fcfda995e7645ad34d3b36d7487d0491709f410c75e34321aeb2252b1c84c107', NULL),
(560000000000010631, 'aini1314', '0c84304ff578d8677859bf79778b48ad6070815cb569789fb7b830c6d4a09122', NULL),
(560000000000010632, 'ainiyiwannian', '6c71d7e7a92122f38fe92d2ebb6a4035c34d30bb17177a425299b6d880dd9152', NULL),
(560000000000010633, 'aipai', 'f7f974c4a73f13da85846fbd8c36b9a29f342abb0f4bb73c8ca76680f24bdc04', NULL),
(560000000000010634, 'aptx4869', '2e06d4d58006dd575764d8e348c653397cd2c33288d4fad146555ae04291824c', NULL),
(560000000000010635, 'aqwe518951', '8274d7afc9ed845b43ae3e12db348be60cb133a331d47c4bf1a84648da7dc75f', NULL),
(560000000000010636, 'as123456', '378265386553b6efd6b7bbbf86d730490ed5258812f227d39daf1b19f2b34cc3', NULL),
(560000000000010637, 'AS123456789', 'b0b697eaaceadc19a0ce0ee09be3e3a9ba20f5c102ade1880316bcff6775983c', NULL),
(560000000000010638, 'asasas', '140e0d2deeb6d6b1b803087c03821448c95f3be61ffd27c89f6c391a3288a838', NULL),
(560000000000010639, 'asd123', '54d5cb2d332dbdb4850293caae4559ce88b65163f1ea5d4e4b3ac49d772ded14', NULL),
(560000000000010640, 'asd123123', 'c82a6c7f5a58a915814334cda5a2ef88ab8cba922f49ec1623e2248ceabf7ddc', NULL),
(560000000000010641, 'asd123456', '6b9bf1373be4be6755043bc1ce4f54176df8fe83e1ffd8cae022d3d51ef121f6', NULL),
(560000000000010642, 'asd123456789', 'df323eca0b07c0ea245838936c55a6f9fe38e185b8c45141203ed4d0d2975e3d', NULL),
(560000000000010643, 'asdasd', '5fd924625f6ab16a19cc9807c7c506ae1813490e4ba675f843d5a10e0baacdb8', NULL),
(560000000000010644, 'asdasd123', '9cdcfbbe0183b2f1855ee2f7354fb2a8d175b133b227052a095302b4559bf525', NULL),
(560000000000010645, 'asdasdasd', 'd8a928b2043db77e340b523547bf16cb4aa483f0645fe0a290ed1f20aab76257', NULL),
(560000000000010646, 'asdf123', '3b0e98ef7923166602d7a9f327782eea090454960cf8bfe1af3b9c8620cd5680', NULL),
(560000000000010647, 'asdf1234', '312433c28349f63c4f387953ff337046e794bea0f9b9ebfcb08e90046ded9c76', NULL),
(560000000000010648, 'asdf123456', '5b73027b39e7d953ebc415632137a8eace6376921519c516cd9ab74bfe4d897d', NULL),
(560000000000010649, 'asdfasdf', '2413fb3709b05939f04cf2e92f7d0897fc2596f9ad0b8a9ea855c7bfebaae892', NULL),
(560000000000010650, 'asdfgh', '8588310a98676af6e22563c1559e1ae20f85950792bdcd0c8f334867c54581cd', NULL),
(560000000000010651, 'ASDFGHJ', 'f61a20da9eaa68a9f06dbc1710b10ef0a67208b2059b1f576af6deac23c215f5', NULL),
(560000000000010652, 'asdfghjk', '5be0888bbe2087f962fee5748d9cf52e37e4c6a24af79675ff7e1ca0a1b12739', NULL),
(560000000000010653, 'ASDFGHJKL', '5c80565db6f29da0b01aa12522c37b32f121cbe47a861ef7f006cb22922dffa1', NULL),
(560000000000010654, 'asdfghjkl123', '17417ebfe065a26e4fe02b9fe45db1e6905c1b61525b948af94f1cd273d81412', NULL),
(560000000000010655, 'asdzxc', '849760fea0863a753ce531afa5196801689dd4300c46fdda2f249dc26f174158', NULL),
(560000000000010656, 'az123456', '3c40dbd1ff576bd62a8979661944fda8f26ad742c76a75121c43b0fbe172e3e1', NULL),
(560000000000010657, 'baobao', '572886cf6640c751fbf0e0e3d347ab4940fae93f45b89d5634e36e07639952ec', NULL),
(560000000000010658, 'baobao520', '40c6916a4f9aa6c471534fb3284b85bb310fddc6297dd42a042c4dd6e2ea7665', NULL),
(560000000000010659, 'baobei', '3043020e284ca6c4dfe4f6c344ce275b10abf2ba79851e75f62220062252ac44', NULL),
(560000000000010660, 'baobei520', '591c1fce499081d5ebe61f8f3e5d0c4e44b9acdf37602329a9d56138c5838fd0', NULL),
(560000000000010661, 'beijing2008', 'a1fdb5c9c0e8873d6c741d36f86eb27066da83e572ab1dd4597df67ff22ec905', NULL),
(560000000000010662, 'beyond', '5e8818d1dd191490273376c09764e7c1b1c5403c815cbf08088bdabdfe61000f', NULL),
(560000000000010663, 'bugaosuni', '4b2644146c0e6a4e4215f9423bbde434e6129862841d1b944e3a1b3e7e3fd5a0', NULL),
(560000000000010664, 'buzhidao', '3bf7415fb474e7e8419a97391a406a0926af054f8a8908942036c7fb5efcfb7b', NULL),
(560000000000010665, 'c123456', '04b10b1ea8a3db83aa866819302939f8784264a53c6415111579c03c32e46452', NULL),
(560000000000010666, 'CAONIMA', '222c5e926c0eea6e05edc07deeb84de0b7b6d4ba2274d88d24da33effbfec9d5', NULL),
(560000000000010667, 'caonima123', 'b3bcef8790658a72c3c6c3016eb6dd57f0f6ce77ad408b3ba7f050836d9e1b32', NULL),
(560000000000010668, 'caonimabi', 'bbbdd429e9145ccdcdb97d6e01ae1e8854673bb969c747b48459f70eb9abfd88', NULL),
(560000000000010669, 'chen123', '12d2c410aae289aa4a586e6851cc55bd7b8b47d470f5cceef5f08653af8cf62d', NULL),
(560000000000010670, 'chen123456', 'e72c987914e98d00c82547dc16214006ffb623667519e49ca866eea0ac1a2e6b', NULL),
(560000000000010671, 'chenchen', '70a4631bde6b419e872180b3a5410b3e3848ff6c6d4545c16fa853a9cb8ea212', NULL),
(560000000000010672, 'chenjian', 'e5a5c433e23378c722455fbb2b7d7aa5747b051139cd1f482d25e8b298e37c13', NULL),
(560000000000010673, 'computer', 'aa97302150fce811425cd84537028a5afbe37e3f1362ad45a51d467e17afdc9c', NULL),
(560000000000010674, 'dandan', 'b8b63f477f28fd2fc7d3935cd08f95473cbf965e259222689fa5ef120d3cfd9e', NULL),
(560000000000010675, 'daniel', 'bd3dae5fb91f88a4f0978222dfd58f59a124257cb081486387cbae9df11fb879', NULL),
(560000000000010676, 'dddddd', 'c02d8e6211ef7dc5af42085cbb323a122b76905ac098467ea1f85465ec14429d', NULL),
(560000000000010677, 'dearbook', '28288b8f0f76557741de71d4548f8fe844365cce8e6cb6c753ba5bf3beee67bf', NULL),
(560000000000010678, 'dongdong', '162fffe9f429e1fa5d351c11f5a7f2f3031806c5125eeb84993f68738cbb6c84', NULL),
(560000000000010679, 'dragon', 'a9c43be948c5cabd56ef2bacffb77cdaa5eec49dd5eb0cc4129cf3eda5f0e74c', NULL),
(560000000000010680, 'EtnXtxSa65', 'd37973933269e5b5e5ab35f32826ca0458d2dc1bd6fdcc170bd60b2b5c13d507', NULL),
(560000000000010681, 'fangfang', '61d0963e8624588293fff02e074e2b85eea9555965bd42ff1beb800daf2bebf1', NULL),
(560000000000010682, 'feifei', '71c0412deeb2760018f12ba3b7dec3d6de11a489d23faf7f105f6c0b8b686f7d', NULL),
(560000000000010683, 'ffffff', 'e2dbf8f5c4cc151480213d21f95c72aa73a001bce4915b17691ae40952dcd793', NULL),
(560000000000010684, 'fill.com', '8afd56ac520af2a45eb7231e965602fbecf2005452c03ee40eb910ef2dd3a2c8', NULL),
(560000000000010685, 'football', '6382deaf1f5dc6e792b76db4a4a7bf2ba468884e000b25e7928e621e27fb23cb', NULL),
(560000000000010686, 'forever', '2070f725ff1c765b73c498de52bc419377979691f6100de3ed99794aeb40d988', NULL),
(560000000000010687, 'freedom', '13b1f7ec5beaefc781e43a3b344371cd49923a8a05edd71844b92f56f6a08d38', NULL),
(560000000000010688, 'from91', '569668e09d567f273b15a9522d77f3df4e08a807bd73027f71593f9b0e308dc0', NULL),
(560000000000010689, 'fuckyou', '6161b0a284159565a0f7d5df2dd2698b5f87906cd91ff5322caf179b451f5a41', NULL),
(560000000000010690, 'g227w212', '3e6aae5c666f086a38f17b3b9fb871134fafed7667e5cd3e05757a367ba3c43c', NULL),
(560000000000010691, 'goodluck', '735701f285cb9253fcff9649a0f7a09f27e5b5967030ded8baba4c5731683636', NULL),
(560000000000010692, 'h123456', 'dfe1a1dcd7072043bab8adf4e589399a981989f2009c95ebe4e77ce28ba4a15e', NULL),
(560000000000010693, 'hao123', 'f795854190c62eaa436721fb8a68791c1e754d955a21f8dcfbd7c563a2eb611a', NULL),
(560000000000010694, 'hao123456', '661010c7dc52950c02b3da0536bc525e3e567e96435580f007ffeafac23ba819', NULL),
(560000000000010695, 'hhhhhh', '89102c5116c37daa55d86342ce2c47d35b1bc2ed407509732d940ea2901befbc', NULL),
(560000000000010696, 'hotmail.com', 'f81e0a262b0f3b39ec26b0a510c71ccd8612d26943abe338e13f77f3ab1a1a59', NULL),
(560000000000010697, 'huang123', 'ba8b6b55b8328e7e2b5d7354c77ce6e2e9ff550a952219cddae5e5c63ef714c7', NULL),
(560000000000010698, 'HUANGwei', '1882b4902a76e8c86c625e052a430d8dc794a9c12761d7a5456ee9117165fde5', NULL),
(560000000000010699, 'huanhuan', 'ea855fe7ccb8b34b150b71e1a8ce0f004134abc9cfaafe2ff879d51b8df0aab7', NULL),
(560000000000010700, 'huyiming', '3b7e92b7a9eabb6a24fd85ebc53aab63ea2fe7334efd94382aa7559f3c6cd2ea', NULL),
(560000000000010701, 'iloveyou', 'e4ad93ca07acb8d908a3aa41e920ea4f4ef4f26e7f86cf8291c5db289780a5ae', NULL),
(560000000000010702, 'Iloveyou1314', '4321dedfc0e68b778ed64a764e67ea74c22dbd89fab6afc811b5cece53f1f695', NULL),
(560000000000010703, 'imissyou', 'ed96fbff085b798ec40cbd01ca632adc37c70d3d35fdf92d3a9fdd3dfe04007b', NULL),
(560000000000010704, 'imzzhan', '629007611701c6d2ebeee18145a867454a41695a7700ea7129ae567561fad6ba', NULL),
(560000000000010705, 'internet', '3b0fe0d342e9fa16a5c68dbba33f2e63c024f72a9d4c1ce1028570101d5229ff', NULL),
(560000000000010706, 'jiajia', '10efee4f8dab5d7e9c2a4a99ef1bbbcfd326ed75c5ac2f2b8ae8e4c2988704dc', NULL),
(560000000000010707, 'jiang123', 'e9d4ddd17841391092aa54026e25e110fd6558170e012230ca2ee2bbdfdf6417', NULL),
(560000000000010708, 'jianjian', 'a7d23a7bb9f6d3401cb9f174cdf6b456920cb99fd2f9587dfb400338a8ec146d', NULL),
(560000000000010709, 'JIAOJIAO', '707b672cc05c8ef148e7a857a429d7a37551134a10d47bc0db14c7b2b2abea88', NULL),
(560000000000010710, 'jingjing', '1af1af669f28b794eb6d103ded99d1e2349b2390c21076bf5df4a76c0b5bfe50', NULL),
(560000000000010711, 'jiushiaini', 'd9c3bf19abc1983ddaa05a4c4f6ddff3096dcd66ac55043a88774e9754488980', NULL),
(560000000000010712, 'Jordan', '136c67657614311f32238751044a0a3c0294f2a521e573afa8e496992d3786ba', NULL),
(560000000000010713, 'js77777', '61f06e7f01016ad172a4c729f9202761d5e29e732bc7be06beadd3fa671316cf', NULL),
(560000000000010714, 'killer', 'ed45d626b07112a8a501d9672f3b92796a6754b8d8d9cb4c617fec9774889220', NULL),
(560000000000010715, 'l123456', '2a6f8457bf8a8ce356e2ba396f3109dd4a04f55e9947667b2d3a13f0f6310087', NULL),
(560000000000010716, 'l123456789', 'df1afa1201251a142a7630a047b56ed9667ac59694967e69d9ea421166eff3a1', NULL),
(560000000000010717, 'langzi123', '74766449bdc1d9069a3ae3dd672392c614e6fe58a83c3a0f6b2b4b947255a561', NULL),
(560000000000010718, 'laopo520', '05972d8402b17113dd28d73755d6c93e179f3f9764a4914aa99f8a74573476a6', NULL),
(560000000000010719, 'LAOpo521', 'de100ae4450a9192bc7eb16434326908d40375fb562e7e70abf2ca8754ec19fc', NULL),
(560000000000010720, 'laopowoaini', '74e01daa77dd2aa11e6d72a00ff231227276fb072a2a591c1a067d4cbe4b67ee', NULL),
(560000000000010721, 'li123456', 'eb5a2e0733495dfb09e3398c695e3379bb698f98950ae3da8be96cbb65507fa3', NULL),
(560000000000010722, 'liangliang', '9eaa52854b329f731e3670459fa56287f0ec21b6d4daeafa363d1910fe741dee', NULL),
(560000000000010723, 'lingling', 'dabb251bac09237605b586fac017d8a371a28eeca338efabd5a5b39c1b7020d2', NULL),
(560000000000010724, 'liu123', '08fcebb5695ed7a5d0e0a0e1192247209e1842a5c0343cc1d68e4116f929a7f4', NULL),
(560000000000010725, 'liu123456', '93d974d472940f364f66f9ed9a1b86d460d6019edc880dad6c2231656bf8a42d', NULL),
(560000000000010726, 'liuchang', '594f5c6fd4a3b9de1c8580b1bf5ba038cf26d69c92ab54bd2cb6e81f364e125f', NULL),
(560000000000010727, 'liuyang', 'b237596964ee2b6a9b392ebe2aa18cb32d6ac1f56bdca103264aa82c6254ea7c', NULL),
(560000000000010728, 'llllll', '9e8bfb3d1a73b03e453fe7315844f3471b0937127b14d24ffae4af47cb1d3d9e', NULL),
(560000000000010729, 'longlong', 'dfeeff5e6648b4482e6a92df134a9a0914860e76a5256b62d49c7d5b910d9144', NULL),
(560000000000010730, 'LOVE1314', '92543c57bac30da66e4e5b4f75c14a5998baf0d2b574f4b860d050bb90910ff6', NULL),
(560000000000010731, 'love520', 'a978c46efcc54ec3d3ee4102f3ea4a4bf8d1d8cc22e86a828520ed4250aa17b6', NULL),
(560000000000010732, 'LOVE5201314', 'a00f68aa604dc4d1930ab6b23b10172ba032c98d3843da0153e6cf67fa7965cd', NULL),
(560000000000010733, 'lovelove', 'fa1baeb8e6f5c28f26997f63cc08bf08ff2632a58a578b8b971dd24d5c7d7863', NULL),
(560000000000010734, 'loveme', '78fe3f05768ff3a95c74ffafe366cc3474022d925ad5593af733bf8ac1ab0de6', NULL),
(560000000000010735, 'loveyou', '848be944013cc3374ddfef3655d6816c060610161c835c71a9665e6dc18f6542', NULL),
(560000000000010736, 'm123456', '5e0041ffb2ea608962fd639ab01c60f12917d19120ebcb6e937ffd9996cae563', NULL),
(560000000000010737, 'maomao', '8a8588ab26e8cfc9995b9a43b953ff5e4a428507637d4c3d328b4a6a2287f49b', NULL),
(560000000000010738, 'meiyoumima', '00a9d757778a82bf08ce04e0828432ce5e09879169fb6b227401a6f596fd61b7', NULL),
(560000000000010739, 'mengmeng', '32dab400ca6bd2b2f7fa5d766733f81485a0614543d11713a0cf707942bb6243', NULL),
(560000000000010740, 'miaomiao', 'e814093dcdf516df701bb6954762385211a33e19376d2e939facae8b7187c3c1', NULL),
(560000000000010741, 'michael', '34550715062af006ac4fab288de67ecb44793c3a05c475227241535f6ef7a81b', NULL),
(560000000000010742, 'mingming', 'bfdbce13daa7c3842498d81e294846f828d5a74eedcee175db8a4433e5eb10b5', NULL),
(560000000000010743, 'mm123456', '982a6acc58d7ddaf9facfae948f49d8f10cbac25157df9c84fccd43a10d2df2b', NULL),
(560000000000010744, 'mmmmmm', '4378c5895e7ade691792a8bbed89dbfcbea257a8949a3b1132a875e67aa45583', NULL),
(560000000000010745, 'mnbvcxz', '2e6887c8745b28fce2e729491ab7e4c63b527baf8c76a649f277623af34be211', NULL),
(560000000000010746, 'monkey', '000c285457fc971f862a79b786476c78812c8897063c6fa9c045f579a3b2d63f', NULL),
(560000000000010747, 'mylove', 'fac445f594a545116747701d3a307109432735698cabeebf65f09ac5d343d78b', NULL),
(560000000000010748, 'NIAIWOMA', '4277c14833aeb9dc531b8fc92567fbff9cdfccc815020be37508e5e826c3f62e', NULL),
(560000000000010749, 'nicholas', 'a6b70a8a5074fc12ed70d61d05fffa30a5bec120ed140cf009b8a6302328360b', NULL),
(560000000000010750, 'nihao123', 'ff4841ce89cb310dcc283bea133896a1f41b2de9b640680cb9e02dc2766dd259', NULL),
(560000000000010751, 'nihaoma', 'b4c76ff4c2cd802e96dd230b506dbd69863a777b668135ccc94279e06f251f11', NULL),
(560000000000010752, 'nimabi', '1576180d1ff6c1c962e7b586c33e6dd88980752f7ca9f84cf284c8ae92d9a627', NULL),
(560000000000010753, 'nishishui', 'b30ebb5fb027cb7c62667bc278ee890fe85219cd47d42f16ee8ab2f6cb297d3b', NULL),
(560000000000010754, 'nishiwode', '53ad98aca9b5b683cfe54e1b62f87594a8fc09f0d7390f5d346bc8530a1912e9', NULL),
(560000000000010755, 'nishizhu', '8d7b594968b0c2592b63fe2001aa211bdf67d84770155d51c4add42a01c77abe', NULL),
(560000000000010756, 'NULL', '74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b', NULL),
(560000000000010757, 'nuttertools', '9ec5dbef87db3a8350efb684f69ea015d8f83935843cfd9b92679088cb79b13f', NULL),
(560000000000010758, 'operation', '9bf5a24e4aa779981ac41f1a0f8713ec758e12df95fa51866869849c06e51175', NULL),
(560000000000010759, 'p1a6s3m', '32ff00708ff4221818fd2201ad443a8421c26e0cd1870ee0046f825fbbc710c7', NULL),
(560000000000010760, 'password', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', NULL),
(560000000000010761, 'poiuytrewq', '091364025ac80949dbc4413fa467bc2035f094a22ffdc95a64e81b10b144b4e8', NULL),
(560000000000010762, 'pp.com', '96f768feef4aadd262f1ee617f00e6497b1689d28fcd5c71a7d0dd812fa3ec08', NULL),
(560000000000010763, 'pp.com@163.com', '29185c3af5a06b8fda8df2ff9ac98df6e4b8aa795f28954f75d094feb4c2a20f', NULL),
(560000000000010764, 'pppppp', '891e12e156d8c6609c6d5f3e04b2fc8da6d9ff3d7e9f906314c0909da69637eb', NULL),
(560000000000010765, 'q11111', 'f152bfd2d43c3cee70d15af629272b3762be1f0a420c72ee7b94a44a2e590894', NULL),
(560000000000010766, 'q111111', '64aaa99bb514fbb309a0b7942f6807fb6300f2dcc7e7356545ad674270162961', NULL),
(560000000000010767, 'q123123', 'a7893b84a5cc4140b0c877b20b3b684f6a08bfe6d70d1ba809addade3ed7e911', NULL),
(560000000000010768, 'q12345', 'e941e808846e10729bc2f5f87fb6c0fc6ae4e734a248b904dadc5fd0bbfd45c2', NULL),
(560000000000010769, 'q123456', 'e48299faab515b91dc03173edcafa1c11010478c422e09b4cc80f5ab44b62f61', NULL),
(560000000000010770, 'q1234567', 'b5ffb38b45bac921375c80735e8116e003ee09e4c38d795a08ce925ea35fcfa6', NULL),
(560000000000010771, 'q123456789', 'a96a19c44d6ded38f431ea567a993c21c89c1680e9f7590aa3a073fe9435c438', NULL),
(560000000000010772, 'q1q1q1q1', 'ce1f9715820e766498685700f9c8fd34a20f6e91d2aa2811b6e6148c2de47984', NULL),
(560000000000010773, 'q1w2e3', 'ae5a853873043c7b011c6300c464d8d4014bf833697a3c01817d83aa91a53166', NULL),
(560000000000010774, 'q1w2e3r4', '13a5c202e320d0bf9bb2c6e2c7cf380a6f7de5d392509fee260b809c893ff2f9', NULL),
(560000000000010775, 'q1w2e3r4t5', '23b5ed29a1e8409f70644e44faebae79ae687318efd719d9af29f8496b016a81', NULL),
(560000000000010776, 'q5201314', '4611f4efd2e559b07dda6f76cfb3263515520d0ddde5abe93bd30af151841f34', NULL),
(560000000000010777, 'qaz123', 'd39bda2bb8268719d4bbd925b4940fd8209cb4688723bfdd88f64bbfa863e721', NULL),
(560000000000010778, 'qaz123456', 'a34d8db5b051887be5491c8f11eaf352b0ef11f0fb74ad00d12d2ae1cffd2635', NULL),
(560000000000010779, 'qaz123456789', '289d2fc2026724053daeda1448edc2a9202811c7a5a887caf3f59d290dbc6b75', NULL),
(560000000000010780, 'QAZQAZ', '11c8f75cc035a5b3c3a84ec6812e832d66b7876a0ee798e7b39620244b557ab6', NULL),
(560000000000010781, 'qazwsx', '88b1cca59060320e5e5662a7da636884eb7580f4dc7e22cfb6f88b8f99045a71', NULL),
(560000000000010782, 'qazwsx123', 'd2b80cb169fae74f334bbf5cd29ffa03740279f03bff8d06e4e49cdf9b07a4e4', NULL),
(560000000000010783, 'qazwsxedc', '80d41c54a8ce6d26ae0bdd509db6b187140cae39b4b771269a0d006b0620e2d2', NULL),
(560000000000010784, 'qazwsxedcrfv', '35f91566a687c0b374663048fc6eab9cf7a0513e8f096cdf1ca2ba6986fa8f23', NULL),
(560000000000010785, 'qazxsw', '9d777935627a29c77604c57273520eb42635fd1847d2eeea1e7441fbaeb26253', NULL),
(560000000000010786, 'qianqian', '9ed9b3a9018dba561cfd17f4d49a2229dca0b729f6d287bd32ea81fde189e0a6', NULL),
(560000000000010787, 'qingqing', 'd21e136122b474e1e06200d146f10d30f44f842fba214e1d9df837d49f2aa5eb', NULL),
(560000000000010788, 'qiulaobai', 'c57aa9a9ba6f784d3bcb0281b925f787841931cf29e47ebce06c1eff476a74b9', NULL),
(560000000000010789, 'qq000000', '8a8b6f1fc1e9b20cc0d962534cca87250681d7f11b703f3005a367106cd1db11', NULL),
(560000000000010790, 'qq1111', 'bce53af88ab3820d8203e43a5cba3e737878477ebf07ec3bbfb568d1fc6c0012', NULL),
(560000000000010791, 'qq111111', '3d868cb60d8b3bd1c15a70fb764440735c6a0d848a980b9fd9457956378291e1', NULL),
(560000000000010792, 'qq123123', '84e4ed1a9b5bb88e0e1f0a3a8b081aee0456975e948565c60773337ab57456d7', NULL),
(560000000000010793, 'qq1234', '036cededec1d9546885dbaa2b48359504d767b5631dc28caa8fa1f7a14d63cef', NULL),
(560000000000010794, 'qq12345', 'ad1df75f3d28a8b347456cf166df2e3bb9638cc72eb813bc693eab00ba88ae3b', NULL),
(560000000000010795, 'qq123456', '09c78756f66334dcc2be3dddbd3125d80d16fd20aa14eaee5922d910eedc0d8d', NULL),
(560000000000010796, 'qq123456789', '30ed8887bd84751275719b2362c1e92b5bfd0d65c1ff75e249297d5c2c644133', NULL),
(560000000000010797, 'qq1314520', '1de02c565f6bba4779a824e46ea3edb7ce36624f7fd64933dec9fb4d37fff611', NULL),
(560000000000010798, 'qq5201314', '492947fbb54ce969aa674bd2f4b88d75afaae6afb80d85c1c145e2a0b8718bfa', NULL),
(560000000000010799, 'qq66666', 'f182c5cac403d16e7e317f1c29bbcc0149e3c8025dc1741d4ac249ff8fc34704', NULL),
(560000000000010800, 'qq666666', '4546d40422517965b0e7d0d30a772ab0e2919d2331d11c11b4ee5455f3b91ac9', NULL),
(560000000000010801, 'qq7758521', '811cb0e58473f18c02d0e009049a66fe1ccf23fd763908e1acb5c8a3f77d902d', NULL),
(560000000000010802, 'qqq111', '396ce936e73c0cd7e270f1a827dc8d5e6cb11385e20e3bfc1d0ea623c135de3e', NULL),
(560000000000010803, 'qqq123', '9fa5982eff3f80653e213359e801af90aa8b705c238ace26f3911161d3a02217', NULL),
(560000000000010804, 'QQQ123456', '429a6ba846dbf9ac63e940c736484f13dc31f7fc65f1f1eeecb8b3a7d2022f6f', NULL),
(560000000000010805, 'qqqqqq', 'b6197fe0d62a4e463edd2925382d4d268c4fce0859378682608efa4fda326f26', NULL),
(560000000000010806, 'qqqqqqqq', '0c88cc86a73aa7c4058ff7c0e31d6f105c0ca3575d5a68e060e95d9c84a607a9', NULL),
(560000000000010807, 'qqqwww', '34bb6fd1751d21d644e6f966882a5f2e50cd26f6502844d38a164a6aac2edada', NULL),
(560000000000010808, 'qw123456', '22177b6f2afa998f2a75cf8ad35227bb659a047a31558b876479530eb267a660', NULL),
(560000000000010809, 'QWASZX', 'b6196f7784bc3acc076eef7dbe22b39d7c8efd5be804685de6d5488013df79f8', NULL),
(560000000000010810, 'qwe123', '18138372fad4b94533cd4881f03dc6c69296dd897234e0cee83f727e2e6b1f63', NULL),
(560000000000010811, 'qwe123123', 'd4355bd37616187bd0c655ea8fa90efe6ee3123f54df2bddfdd8dc2e05e840a1', NULL),
(560000000000010812, 'qwe123456', '0b45af9d0463c40f5a330f41cb59fbe60dbe8f72cf8dc76742619d29ecef6945', NULL),
(560000000000010813, 'qwe123456789', 'e695ff30a01d15f0b67f5428fc59cb10bb45845d6bff5c1aa74ad0ab9ca0fb09', NULL),
(560000000000010814, 'qweasd', 'a1bd1312d23002be258c9bb4642bbea77580353869a8ee8844e6940b7e0278b7', NULL),
(560000000000010815, 'qweasd123', 'e6f458074668ad5d8f55d414bbe2b908fbb46909ea36b19c8548045bbeeb164e', NULL),
(560000000000010816, 'qweasdzxc', '1305485a712608fdc4d2fd1780c72919f2f54cf288525814bff7120737f6ddad', NULL),
(560000000000010817, 'qweqwe', '3cc849279ba298b587a34cabaeffc5ecb3a044bbf97c516fab7ede9d1af77cfa', NULL),
(560000000000010818, 'qweqweqwe', '0d1ea4c256cd50a2a7ccbfd22b3d9959f6fd30bd840b9ff3c7c65ee4e21df06d', NULL),
(560000000000010819, 'qwer123', 'b10249b3b99c35a0fca54c73c2194879343ff6ebd35fe937b20f4e528522bb8c', NULL),
(560000000000010820, 'qwer1234', '4d4f26369171994f3a46776ee2d88494fb9955800a5bb6261c016c4bb9f30b56', NULL),
(560000000000010821, 'qwer123456', '5a012756f3fe06210fe8f3afca3176541d8ad232e9d34aa5aedf7c19715ad4b9', NULL),
(560000000000010822, 'qwerasdf', 'c20c769db9e1d1118841c9f453800d03dad2d9854e2ba589b4e0fb6c0f42f886', NULL),
(560000000000010823, 'qwert123', 'bcd8af9004d8b5d24647b3f3cd5759fc8d3dc72a5c3fe932d97be2960d57b0e2', NULL),
(560000000000010824, 'qwert12345', '777524f0cf9c792596eb2b3c57801dbd37b6999910d7e693922ab25c9193faa9', NULL),
(560000000000010825, 'qwerty', '65e84be33532fb784c48129675f9eff3a682b27168c0ea744b2cf58ee02337c5', NULL),
(560000000000010826, 'qwertyu', '1411242b2139f9fa57a802e1dc172e3e1ca7655ac2d06d83b22958951072261b', NULL),
(560000000000010827, 'qwertyui', 'a17444550e2c127b02ea1c197bcffa422c21713040f53d5c2ca7925419bccf7f', NULL),
(560000000000010828, 'QWErtyUIO', '2e37bf4bd9144b2abc03042f6e8789d084be078eda8e74c24f3f541b511d8c2d', NULL),
(560000000000010829, 'qwertyuiop', '9a900403ac313ba27a1bc81f0932652b8020dac92c234d98fa0b06bf0040ecfd', NULL),
(560000000000010830, 'qwertyuiop123', '8bf4dec545e105bb54dafcfe6436b67ab8bf0c01d7b575d865810661b858d86f', NULL),
(560000000000010831, 'qwqwqw', '94911c5176be2e43a09fd03a55732fef0f56a61fcf56d6486d7b21dfae5cdb01', NULL),
(560000000000010832, 's123456', 'f2e5aafc64a04ac704c644ce38c34d1d7f7493561687c66e43eeda8186744134', NULL),
(560000000000010833, 'shadow', '0bb09d80600eec3eb9d7793a6f859bedde2a2d83899b70bd78e961ed674b32f4', NULL),
(560000000000010834, 'shanghai', '713ec46512c8f37127281f0e10c5eecb7f41e493c29aaebfc3105e919131cc43', NULL),
(560000000000010835, 'shangxin', 'd554d3e525d5915e33a110016d519b2f48157fb2c75b6d3da0d5a93c211d1a96', NULL),
(560000000000010836, 'shanshan', '89087a0dbc99f426e7d97effc9a7264dc5ec7e25d5a44e00d2eb7482262f2eed', NULL),
(560000000000010837, 'shmily', 'c8802a7d7cf43f2b7dd2270c05dc4b1aa8ab062b7657f67d17eea6c142016377', NULL),
(560000000000010838, 'sj811212', '77579f020518986b92fd1bb00e9f0bfb9a007ca3db7e95e5197a4023a60e3af4', NULL),
(560000000000010839, 'sohu.com', '1b1791de099844c521306dc1f01f0a862e2c690bc1869d82b167c44ca03d9003', NULL),
(560000000000010840, 'ssssss', '0a1b086f072513ebb1d3d715166583135b706781ce4948cb1eb90b9837eb5707', NULL),
(560000000000010841, 'stryker', '09792abb14956e93ecf2a709affde76344ca3ed396e57ffcbf65436cce83fe3c', NULL),
(560000000000010842, 'summer', 'e83664255c6963e962bb20f9fcfaad1b570ddf5da69f5444ed37e5260f3ef689', NULL),
(560000000000010843, 'sunliu66', 'b4cb3c4de0367181c0d4a397341b0e2c877dedb959622ce48224021bde822d2b', NULL),
(560000000000010844, 'sunshine', 'a941a4c4fd0c01cddef61b8be963bf4c1e2b0811c037ce3f1835fddf6ef6c223', NULL),
(560000000000010845, 'superman', '73cd1b16c4fb83061ad18a0b29b9643a68d4640075a466dc9e51682f84a847f5', NULL),
(560000000000010846, 'tangkai', '24f19ca3b74f6a248f9844f171a9d2e8a81259b761e2dfca6de3229f5a9b2ee0', NULL),
(560000000000010847, 'tiancai', 'e9bd90f62b6c41dd98292bbc1e898aa24bbd245a5fd38640cf4a98b76b3ad9b2', NULL),
(560000000000010848, 'tianshi', '6211d798099ad9f91bcd657d4dd723c6f8def2fb84416ccaaecd75136630c672', NULL),
(560000000000010849, 'tiantian', '8308f6f099a5c0da8ea8c8a28cad83a5d62c2c53da03e4f18a8077dca4033535', NULL),
(560000000000010850, 'tianya', '9c265e6f022b7dbae8a8a3367832522b0224c3b7b1e6ea9a7643dfe7ca1a562b', NULL),
(560000000000010851, 'tingting', 'da320561ec9935bfca8dcc0647db0467ef8895924ef0de5b9e0af56c121b7dbf', NULL),
(560000000000010852, 'tom.com', '7416b69c97ec425fbc610ece251a00303c9ca2f62b7d0069a34c48e2d80ce135', NULL),
(560000000000010853, 'w123456', '6446effe9166cb60d969cfd9784e7efe8980f7bf84613eda0d6b1ef200ffad94', NULL),
(560000000000010854, 'w123456789', '75d96a26cd17c2d94204bbfa8e2fc9a3611e44d5e4726e96f29a72b905c78855', NULL),
(560000000000010855, 'w2w2w2', 'c95643b3bf1a2e6de3762cb6e1a38e09c5afcf10f80acf775d29021fa498741e', NULL),
(560000000000010856, 'w5201314', '40d5ffe9ca3989b6f7dfe55932e837d93b6fd87b6db305cdc52bd1c70c5066dc', NULL),
(560000000000010857, 'wang123', '4a3e11c20cc03038b9549fa239bbf403968799c134436e7e58162a0a4f7644f9', NULL),
(560000000000010858, 'wang123456', '0854e71321cac492c507370fc28d707ef08c2c9c5a4b21550ee5734bf4494a0e', NULL),
(560000000000010859, 'wangchao', '26c944c9edff1bbc6d950fe989cdb63d82d0b5486cb15dc2076bd50c4b363407', NULL),
(560000000000010860, 'wangfeng', '1ab82673bcb3523d132a656ca5e29e997968b959700716aee27eb927f2911a2f', NULL),
(560000000000010861, 'wanggang', 'a0b7b37884ee5fd3c7a8d60b80fa604fc509fdb34108857e36a5be9f6f4d4961', NULL),
(560000000000010862, 'wanghao', 'a4ce4d627af342628721013a787e8185d9da95ab0ea489cdc10bfed88323a7d7', NULL),
(560000000000010863, 'wangjian', '83eb9a86571b12c57746f08fa3268258957e19a5c96a7e39503fac6bb21eb983', NULL),
(560000000000010864, 'WANGJING', '04add76189d75a34a1c72335724e7db0cda4230d2933d04159ce9e9f61154530', NULL),
(560000000000010865, 'wangjun', 'd58a67277e5ef2e664dbaa92d415c1eb9eb0e4163a3a73adb88dca0ba8d99392', NULL),
(560000000000010866, 'wanglei', '41a00c2bec846e201aeac54ebe9d6a0ffc75fdea03d57e719ee8ddfe98b16879', NULL),
(560000000000010867, 'wangliang', '7e512832e4f5dd9bad057b3c265a51215aac74d5f06dab74d1d548604072d3c5', NULL),
(560000000000010868, 'wangpeng', 'fd009e5e04a5112d16a4a0066d93ea07165aa43fbad78d69718b5fe6e82791e4', NULL),
(560000000000010869, 'wangqiang', '6440d617b435234b6c6eeda5296d303f8537616900ee9e35802f009f028e1a91', NULL),
(560000000000010870, 'wangshuai', '060328cb263850c3539fca525f4b62bebcc35cd47c776d424300f4cfecc8ad40', NULL),
(560000000000010871, 'wangwang', '53d18949d7f3d47cef17856550090b2026a7b759670e7e7c8afdc862af38ad23', NULL),
(560000000000010872, 'wangwei', '40f4ca1c058893232cdb68dd58e09146094e46004738b86e69866c06a5e4f40b', NULL),
(560000000000010873, 'wangxin', 'e453e48b56479604c44c4b5a7660c494805fe8e3258289adb09889a9a358cbb6', NULL),
(560000000000010874, 'wangyan', 'f7acb2230f6fd331a9bbb77ca38c64bc63228841c7aa7c0babf080d881979f93', NULL),
(560000000000010875, 'wangyang', '38968164e5b8f0668ba931af2894fdd949ba50dc292c9bbbdc9bdd9cb25c0403', NULL),
(560000000000010876, 'wangyut2', 'f8de24c4a9acd2721b6482cc567b15ac2cb1f17fe2bbad096d6c617ba5fc09de', NULL),
(560000000000010877, 'weiwei', '5dcf803d4764a9403dcd18a1e2beb4b813500283848f02c79c7a3825de4a5c95', NULL),
(560000000000010878, 'welcome', '280d44ab1e9f79b5cce2dd4f58f5fe91f0fbacdac9f7447dffc318ceb79f2d02', NULL),
(560000000000010879, 'wenwen', 'c66c57a4c7f6764a5ad5884c45d02ebd6d9aebf283cef2c5b0e9b44643e99f71', NULL),
(560000000000010880, 'windows', '340d600392818df2413382dc7d8325c360d83ea49a262d31760348484bbc10b5', NULL),
(560000000000010881, 'winner', '4221a0fc3dfbfd830dc3a13f6c72d233781179bea27df532ff903f3abdba5586', NULL),
(560000000000010882, 'wmsxie123', 'ce65095ed94f482ed3beff717d1c8f66c7839d5df2c6778c37b71c9045b8db99', NULL),
(560000000000010883, 'wo123456', '241f01981d39dd1eafa71e87d7d49581b8d3be70aa2e02ef733222b66bc2043a', NULL),
(560000000000010884, 'woailaopo', '291159ae97a05205c91c4e3eae476789ab9249cfb3c914347548834760ea9e1b', NULL),
(560000000000010885, 'WOAIMAMA', '60c37d7c755fe1b9fc74b7166019f93ab3e22ddb83ea403ac33d87cfb2f0dcf3', NULL),
(560000000000010886, 'woaini', 'f72f3302519ee6484f54a7cbbbdf07e3f92543545a3d3174ee909b23807fdc1f', NULL),
(560000000000010887, 'woaini@', '5047e9a5aa71d85d8e2edaa276a56193047d179f5c70bab477430ef29e10a1e3', NULL),
(560000000000010888, 'woaini110', 'eacd9a06b87666d11fbf0cfde69ca26382ea4d0811c4d0ae74152742f7ff8f5b', NULL),
(560000000000010889, 'woaini123', '0ae6702a4543bbde0a673648505ec0ed829b84bd307f9f9830c90cdbab96acfd', NULL),
(560000000000010890, 'woaini123456', '9ba5281f0dbf4212bf9f47245428b437d46ce210ded9df4fd9e4d3de49a8d496', NULL),
(560000000000010891, 'woaini1314', '4a550a0a8153db5abe7f248dab22772a32db14dad56cfbb3ddd05e0123ce7b74', NULL),
(560000000000010892, 'woaini1314520', 'c7e5a0607c70f3b64f2be2d6d676382ce4e3f990c5833a4ad85e2c73050edbbc', NULL),
(560000000000010893, 'woaini520', '472389df0e5e5bc13d1a77dae316a53494a93121671b1d268d691da264620417', NULL),
(560000000000010894, 'woaini5201314', '6b491990f6e819d2a4a36b5513277b57c77d537cde3ec47ce3025948da617f44', NULL),
(560000000000010895, 'woaini521', '4140d786d98f29574ce61639865837dd3737c599fdc440f4617652bf42f8feff', NULL),
(560000000000010896, 'woainia', '2cb8d392aa8d8838dfe1bfb72e8f590b628be67a848c1e9f91cac06c6ff821b6', NULL),
(560000000000010897, 'woainilaopo', '2ebaba37abcc85657ea2c888847ff098738dae1c559b809d926d141a261b8424', NULL),
(560000000000010898, 'woainima', '13110e36017a7e8672acc1bafa1551c53656a0a72b40cf59fbc4b6316d5a9d8a', NULL),
(560000000000010899, 'woainimama', '744a3afd5e0b1bc88ea6f8a679d13c9a82cb2d22e42ffff959884ee8d3b74dd7', NULL),
(560000000000010900, 'WOAIWOJIA', 'aaedb5fad2c8803472d946b2071f03d26cbf3016001912b6ba5fa515f4fa467d', NULL),
(560000000000010901, 'woaiwolaopo', '57ee06ebdfaa79c4b9b9d86eb2eeab18df5ffd302120caaf91d3f07d5cb5249b', NULL),
(560000000000010902, 'woaiwoziji', 'c9c41a9af279102b3941a2a7a1980f2d70aa5641118bbb55b57554ea13e26a22', NULL),
(560000000000010903, 'wobuzhidao', '47a20ec42aa1d4473eb5f0e82a7fb2ebb366f119dae0ad331d65b8327142899d', NULL),
(560000000000010904, 'wocaonima', 'df4592465e918187264cf0a973db1bbdb169acd6a2cddaa9c74d86a500cc2dc9', NULL),
(560000000000010905, 'wodemima', 'b4b58508b1f008154f84c2a456be4ece46e4d23882a9063cb3dac1a7c4c97776', NULL),
(560000000000010906, 'wojiushiwo', 'b0a41ba941d8e27465340a9900b11552c60a2eec9341920b222e6530fa582562', NULL),
(560000000000010907, 'wokaonima', '8a776f2a5e00a4996bf2313a748ad40df070f6eeef18acfe28ec1f95f2c25c37', NULL),
(560000000000010908, 'wolf8637', '54dd0ca96893c566b662c10e1a76b5fd4f7abbf03c80175c9a3cbb7c12a89c11', NULL),
(560000000000010909, 'womendeai', '1e567ab9623ed017b34c065d16ec8ac56cf77546d499913cf7e0102996091ed1', NULL),
(560000000000010910, 'worinima', '89dbeeecf39cae3bccc94495fa64ce46e505a26d89dccf2ad599dfd4bd766368', NULL),
(560000000000010911, 'woshiniba', '0670c6c36d52df4394aab933b8768b6db43a6fbcdc528a7fad0521be37ab892b', NULL),
(560000000000010912, 'woshinidie', '80452dbe4c974b6edc888156305dedd8dcd8965f0f771521eb435571e19b8e8b', NULL),
(560000000000010913, 'woshishei', '281a9544beea135408062ea8b79c129cb8e62193f83c4eded6f3a7a0297ae9a1', NULL),
(560000000000010914, 'woshishen', 'aa0c95de59bb668453fc78b9cdfd38555d0a812109029f7b36ff607383319dc2', NULL),
(560000000000010915, 'woshishui', 'b3d9b5ab4d3f5892ccebd43f693b3b191dd8519996d65d7a9d3dc163f325235d', NULL),
(560000000000010916, 'woshiwo', 'dff07082681b1a5208c28272644ee9e3c487f19883f52921a9151ca196784474', NULL),
(560000000000010917, 'woshizhu', '2d0474575f161d06cfdd105ce6788e3c57e0d9920a8fb6bcb2d85035ecee5853', NULL),
(560000000000010918, 'woxiangni', 'a630eadf5dfbdadc7eb3673617cdb3f1c303cc8ca35379c70b3294f88d76b352', NULL),
(560000000000010919, 'woxihuanni', '4fac0d48497530d41cf5fc196a60b0f90d262cc1713990c790a61aa34c858856', NULL),
(560000000000010920, 'wozhiaini', '2e3d26ab0ea3018be32f50541735fbe7ef67d42dc75f00c0b4d1eec6e51179a9', NULL),
(560000000000010921, 'wpc000821', '9bb9aad0100425faf7156a950898bed8dceb85a68c4d554247d9a88d6ece3d5d', NULL),
(560000000000010922, 'wu123456', '573059d96c064e959bf658070651849e685139be850a10e50843fe7ffe29e00a', NULL),
(560000000000010923, 'ww111111', '8e03978cd42e8f96073f0bd0564a42e98175a6ecfe9096d64815cfaef1e72b30', NULL),
(560000000000010924, 'ww123456', '0519b9eaf15072cdaa4ac8a8d508f0293e533db377c91e30d8c75ea7380ddfbb', NULL),
(560000000000010925, 'www123', '51152171c7e972b2ef62a04a90e2c7af7df79e1a83be309f343037ad913d02bc', NULL),
(560000000000010926, 'www123456', '59c630335cfb40f8c05c4cffca8e13c50929d42df509cef127e545425ece8166', NULL),
(560000000000010927, 'www123456789', '612d29e874ad70d2c64c2ae0c00d6f874759de5d7f01366c40053efe322e7bb2', NULL),
(560000000000010928, 'www163com', 'fe286aebaea8d398cfcdf0b70b797a7bea8d88cd198c878fbf8673efb2f1a422', NULL),
(560000000000010929, 'wwwwww', 'ea3d7d28fd6e362047d6424bc9229093f3a54ed7d92b36f0d2858c70ed8d7005', NULL),
(560000000000010930, 'x123456', '45a17f2607c7ba02673c521881902ff6fc46eb504e4955c039148be2b9a50dbf', NULL),
(560000000000010931, 'xiaofeng', 'd5fac4e9deff1d4cd38c7a64829b537c84eb47bf6747556ec79b583fc62ad61c', NULL),
(560000000000010932, 'xiaohe', '69010bde33be6065da27723d9e6272f7d983aaf50070a4ca1ccfdcb197607d1b', NULL),
(560000000000010933, 'xiaojian', '909d7d0ae6b7bf93823c22be154c4940bb1f048a40a4f998d35ca27a1f5eeb64', NULL),
(560000000000010934, 'xiaolong', '5c6f2af8bfb23a358dfa9acb0863fe518940f9e7b737e41540c35ef03a0e69d9', NULL),
(560000000000010935, 'xiaoming', 'f95dd02f6b3091391e83b53fc0303509bf6178973e4a7a7cebbbf2892e83acaa', NULL),
(560000000000010936, 'xiaoqiang', 'd3ad422a5be27ddfa5682834f0458dc24d9dd0f13d91b67e27dddf3f80161ae2', NULL),
(560000000000010937, 'xiaowei', 'ebfa642a71df91488ca126ba2c21e62abfef16059ca78af06ee1436d57191fe5', NULL),
(560000000000010938, 'xiaoxiao', '9906d9da59fb551b59824eddb1c36dff0fd8d8d7e32de828bbb27f7a926301fe', NULL),
(560000000000010939, 'xiaoyu', 'a8b2eca10c180ef011be6ffdb5873c29e8e22e56a37d92792142f48b497a3bfb', NULL),
(560000000000010940, 'xihuanni', '454fbb7e01b0d0dcd8758c55bcebffcf2042b328a422f8d93167d06340ac9250', NULL),
(560000000000010941, 'xingxing', 'f2ce7d7c04e847b35dafb9edc2d044c7085c308270645b40b56a9df7ed01abea', NULL),
(560000000000010942, 'xinxin', '345da44d8ecc8b2ddba72dc206aa846bd84d4cc7c0939227918135db20584342', NULL),
(560000000000010943, 'xx123456', '5748a25086c554c1cf8f1bf98ad103a52cf6e111ccd0c2e50e23258628011659', NULL),
(560000000000010944, 'xxxxxx', 'b7fb217694ae2d305e766608d250f797daa984e4ac4b5fa638a729be352f2fcd', NULL),
(560000000000010945, 'xy123456', '2a783f04d8a93db475187f67ad00d96f2b65e074b89d394e3300a628ee7e8829', NULL),
(560000000000010946, 'y123456', 'ff9213a90ce7d94c8683f053ee53493e455a40c06768b2914755fb07bd09497d', NULL),
(560000000000010947, 'yahoo.cn', '8213247ee28118e89db8ddc0c1ee33d48c8f45f0d20d54b88c7e646a2abd64e1', NULL),
(560000000000010948, 'yahoo.com', 'b33960790a6de22ca95a61c6d1310b3487304529cb0af6f3e9b80d2e5ddf76b8', NULL),
(560000000000010949, 'yahoo.com.cn', '6b73859c1025b64a4daf36b4c6b9ba4249410f094bf55046a55948fa37b6330e', NULL),
(560000000000010950, 'YANG123', 'd36524ed961d94ad70eeec1239a6ffab39539c391a77ff07bae131385425d08b', NULL),
(560000000000010951, 'yang123456', 'fe722635c2dcbf66048fa9ef4dcf1ec3e301c2f9b942ed523f64814afff82beb', NULL),
(560000000000010952, 'yangyang', 'be604874d2c3a5ca486f6c9fc9ba383497ebddf932a603e898fabc32453ee937', NULL),
(560000000000010953, 'yingying', 'd6bbd156eb36499c3ac3cce3f754778b9f317757ca3f5f52ea51b1e0f8b77d63', NULL),
(560000000000010954, 'yj2009', '272d65e294e7d2ea0ac508dad61b07b11ebec7ff9d4bafdb98ea54ed3915fd39', NULL),
(560000000000010955, 'yu123456', 'e256f9729f2137b0bb5d9ec05d83b78f4e99fefebdda4067bd6286aac7ff9cc3', NULL),
(560000000000010956, 'yuanyuan', 'f760b4ba1f2f85061cc5011e4fe88f95dfeb7d19f1a9eec9d3516c0e77a5aa66', NULL),
(560000000000010957, 'yy123456', '5572d04fd61ca76c15b98c204bf68866a05bf141338f4ddb57e215810911a95e', NULL),
(560000000000010958, 'z123123', '5cc84638878fecc593a79a908e6b5d0858b8e5127167d64a1f5f1424fe972b15', NULL),
(560000000000010959, 'z123456', '9d848d4b8d1f0187b55b68be33b361f939319781a2d517d047a3421cf8329e5d', NULL),
(560000000000010960, 'z123456789', 'be8a5f11c67913858aa213fc13de3cf4f70daf828058b0b76b27064fb8da4a22', NULL),
(560000000000010961, 'z321321', 'e2cf60931ddafc8ec98d51423844afcf5bbd185e2a9dadb4385ac673cb348ec9', NULL),
(560000000000010962, 'zhang123', '81358f158c88145a6e30b9d68abdfb7e2cf92fa2e2fc8b70c3f0ed79035cc7a0', NULL),
(560000000000010963, 'zhang123456', 'a3f462e74cdc4c47e74badf67475cd625b91cdb12f89a5120a2e2bbf67f813aa', NULL),
(560000000000010964, 'zhang520', 'a5d4dc98e5654f003b090b874ede990d4c3eb3482b87e129f03dc67f93b81cf9', NULL),
(560000000000010965, 'zhangbin', 'ce03f55e0ec118456d05690a8c8dfe4d2812340ff164241b79a8cf4641727f9c', NULL),
(560000000000010966, 'zhangchao', 'e94c5dbad38de7f85fad1e1e7ab203811fe79221d921bf02c8f3aaa6beb8f443', NULL),
(560000000000010967, 'zhanghao', 'b363559788e68f47e63b555a760c17df28a75c17cf11c9134c7d31e19675fb65', NULL),
(560000000000010968, 'zhanghui', '26e44e9736c6baecd24464d4f851eb5e34bed866535638458eab606dd908e5f1', NULL),
(560000000000010969, 'zhangjian', '2406b65d46af4f2ef5ed8914abff6d4504150b2962dabbfa1661a5ebb7168a9b', NULL),
(560000000000010970, 'zhangjie', '9c3795e3f9ae5aa0e726ae3044aebf5ab54b66640d842f4afe890d31817461fe', NULL),
(560000000000010971, 'zhangjing', '15edce838e3b73522f6e7dd2eec6474ad586ceaea249077d8d25462a09e1f8fa', NULL),
(560000000000010972, 'zhangjun', '2105dcc19411750fabe4f29155ce8013c36aeed80288a49e3121e2358d3b291e', NULL),
(560000000000010973, 'zhangkai', 'c096df537a6c5d91ceb23ac7210ebd270ff10d4be31dfd0f2d0813893ad99b84', NULL),
(560000000000010974, 'zhanglei', '7b5d462ca477a02cbd684cd6acaf5108592a67c3de79782626ab29f6224228f6', NULL),
(560000000000010975, 'zhangliang', 'c4229ce1f216144f6931c89147b948493e6a500dc6980f6a3c833e28e79fe062', NULL),
(560000000000010976, 'zhangpeng', '9706c060f65a65999b64c27f8d55546415fd34c04e58dce2e1f1d322f3db26d2', NULL),
(560000000000010977, 'zhangqiang', 'c9745c5d8f003a8ba49e242a7f02103e5d6e42665f2fc86cff25613bfba85b08', NULL),
(560000000000010978, 'zhangtao', '5c261edad96dc420fa63b91fce10e8fc36fa8adc055a7356f1e90b4d107ecbbd', NULL),
(560000000000010979, 'zhangwei', 'fe5f545877786858c59393121cdea81956ae6b7caf57e24f9a4d3a7903f81b61', NULL),
(560000000000010980, 'zhangxin', 'bfd0e1efb3568f55a9359111be07608ec39a545dc349a6fc2078ef9f37005a3d', NULL),
(560000000000010981, 'zhangyan', '56f3968d964a9ea75a0475dafc5939ce2fe0cc66279e0d0d2079274e1a832230', NULL),
(560000000000010982, 'zhangyang', '37261d2cd9153e120e01d8b5c3fa7d9600f86e4a94faad7e3da8ca4c58d4e2d6', NULL),
(560000000000010983, 'zhangyu', '0be5b683de41209e2f12258f57c6071317b0c8fcb5bc2d0567005560bb9b3889', NULL),
(560000000000010984, 'zhendeaini', '16cc306cbf0e4fb84c1dc60380dcd449d00c06b4ab67c79741afa4e601f2ad65', NULL),
(560000000000010985, 'ZHUZHU', 'ff842e6c428b31400a2cd5383d05fa08ef9cc0098644c579fbda2b9006d10ca7', NULL),
(560000000000010986, 'zx123456', 'add4862e6bab1f3721401c6c98d5e6f95c7f4c872201c61691101a794e0d40b7', NULL),
(560000000000010987, 'zxc123', '0d81684688d4057da4d9f6df64b28154b68afc2f1946a756302613c92fdd4986', NULL),
(560000000000010988, 'zxc123456', '9ce4a57f38793f914ba1dc4e2c31ab1b7521dab41cd43ba986f0485bd81f5920', NULL),
(560000000000010989, 'zxcv123', '571f5206ffcaf5e66a0506deea9e971059b7d852ac3d23261f1d50ee9d45079f', NULL),
(560000000000010990, 'zxcv1234', 'bef9bc5fb772c39fb335768e85bb188470130a65fe1ee92463827127b4d0670f', NULL),
(560000000000010991, 'zxcvbn', 'e8f56862d74ef5599af4eeca73924bfa44a6773a497af0c29c48e18729ba6ff0', NULL),
(560000000000010992, 'zxcvbnm', '1df1854015e31ca286d015345eaff29a6c6073f70984a3a746823d4cac16b075', NULL),
(560000000000010993, 'zxcvbnm,./', 'f003b373f910f86ad3c021a11b54aceaed7441feabc8cd041cdce572c472d2d0', NULL),
(560000000000010994, 'zxcvbnm123', 'eabc1c385be0b295ec9d66ce5b535849461415b4b95f2c578447dad7b30a2252', NULL),
(560000000000010995, 'zxczxc', 'a0ec06301bf1814970a70f89d1d373afdff9a36d1ba6675fc02f8a975f4efaeb', NULL),
(560000000000010996, 'zz123456', 'a66c83560690291518e2463eaf5722dd23ccc05d5da896a84a8214541d3be5a7', NULL),
(560000000000010997, 'zz12369', '12c922bf17c333f2132ff7ec1bdb7b88d3929bbd1c91190018ee78622bc28a26', NULL),
(560000000000010998, 'zzb19860526', '02a853652e72f46db689c763647eed173cbc751e98badb23e18a829b441807a6', NULL),
(560000000000010999, 'zzzxxx', 'ea047fa1f36a6537a954fbdc483da63160defabba8adcc1c95b15e148be01218', NULL),
(560000000000011000, 'zzzzzz', '95fbeb8f769d2c0079d1d11348877da944aaefaba6ecf9f7f7dab6344ece8605', NULL),
(560000000000011001, 'zzzzzzzz', 'c129db8be8904b40ac21c9cf5d9f5c0e24ef455d1d7a7bbfd7049fc6dc9d2429', NULL);
