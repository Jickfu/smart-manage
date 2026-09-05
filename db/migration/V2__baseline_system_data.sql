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
-- Data for Name: t_sys_feature; Type: TABLE DATA; Schema: public; Owner: -
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

INSERT INTO public.t_sys_file_config VALUES (2082478248768778241, 'LOCAL', 'E:/smfiles/', NULL, 21, NULL, NULL, true, '2026-07-29 22:47:59.740268', '2026-09-01 10:57:48.107604', 1, 1, NULL, 4, NULL, NULL, NULL, NULL, NULL, true);


--
-- Data for Name: t_sys_job; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_job VALUES (470000000000001301, '邮件投递派发', 'SYSTEM', '处理持久化邮件任务并执行有限重试', 'sm.domain.sys.scheduler.job.DispatchEmailJob', '0/15 * * * * ?', '{"batchSize":20}', 'ENABLED', NULL, NULL, NULL, NULL, 'SYSTEM_EMAIL_DISPATCH', true, 0, 'system-email-dispatch');
INSERT INTO public.t_sys_job VALUES (2082857218823630850, '附件对象清理', 'SYSTEM', '清理过期临时附件，并重试数据库已标记待删除的对象', 'sm.domain.sys.scheduler.job.CleanTempFileJob', '0 0/30 * * * ?', '{}', 'ENABLED', '2026-07-30 23:53:53.241535', '2026-08-26 21:53:56.414223', 1, NULL, 'ATTACHMENT_OBJECT_CLEANUP', true, 8, 'attachment-object-cleanup');
INSERT INTO public.t_sys_job VALUES (510000000000000301, '站内消息发布派发', 'SYSTEM', '为待发布消息生成当前启用用户收件快照', 'sm.domain.sys.scheduler.job.DispatchInboxMessageJob', '0/10 * * * * ?', '{"batchSize":5}', 'ENABLED', '2026-08-28 17:59:18.631332', NULL, NULL, NULL, 'SYSTEM_INBOX_MESSAGE_DISPATCH', true, 0, 'system-inbox-message-dispatch');
INSERT INTO public.t_sys_job VALUES (440000000000000001, '系统日志分区转储', 'SYSTEM', '将超过在线保留期的完整月分区转入历史父表', 'sm.domain.sys.scheduler.job.ArchiveSystemLogJob', '0 10 2 * * ?', '{"jobLogHotDays": 90, "sqlLogHotDays": 180, "loginLogHotDays": 180, "scriptLogHotDays": 180, "openApiLogHotDays": 180, "operateLogHotDays": 180, "maxPartitionsPerRun": 12}', 'PAUSED', '2026-08-10 16:19:04.823455', '2026-08-31 21:18:52.196357', NULL, NULL, 'SYSTEM_LOG_ARCHIVE', true, 1, 'system-log-lifecycle');
INSERT INTO public.t_sys_job VALUES (440000000000000002, '系统日志历史淘汰', 'SYSTEM', '删除超过历史保留期的完整月分区', 'sm.domain.sys.scheduler.job.PurgeSystemLogHistoryJob', '0 40 2 * * ?', '{"jobLogRetentionDays": 365, "maxPartitionsPerRun": 12, "sqlLogRetentionDays": 730, "loginLogRetentionDays": 1095, "scriptLogRetentionDays": 730, "openApiLogRetentionDays": 730, "operateLogRetentionDays": 1095}', 'PAUSED', '2026-08-10 16:19:04.823455', '2026-08-31 21:18:52.196357', NULL, NULL, 'SYSTEM_LOG_HISTORY_PURGE', true, 1, 'system-log-lifecycle');


--
-- Data for Name: t_sys_permission; Type: TABLE DATA; Schema: public; Owner: -
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
-- Data for Name: t_sys_menu; Type: TABLE DATA; Schema: public; Owner: -
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

INSERT INTO public.t_sys_user VALUES (1, 'administrator', '$argon2i$v=19$m=65536,t=2,p=1$YX7MmbacZUT02bWnUBFzLQ$yhCo/keSWZm4rO7TQ60+9WsnaQoXNWM7/I6TYGXlQgw', NULL, '18888888888', '#276FF5', true, '2026-07-27 17:59:01.985745', NULL, '2026-09-05 18:42:13.003955', 1, 39, false, '管理员', 'administrator', NULL, NULL, NULL, NULL, 0);


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
