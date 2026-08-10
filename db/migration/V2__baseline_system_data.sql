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
-- Data for Name: t_sys_org; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_org (id, name, number, parent_id, sort, create_time, update_time, create_user, update_user) VALUES (1, '默认组织', 'DEFAULT_ORG', 0, 1, '2026-04-14 13:15:18.817269', '2026-04-14 13:15:18.817269', NULL, NULL);


--
-- Data for Name: t_sys_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_user (id, username, password, nickname, avatar, email, phone, theme_color, enabled, create_time, create_user, update_time, update_user, version, password_reset) VALUES (1, 'administrator', '$argon2i$v=19$m=65536,t=2,p=1$YX7MmbacZUT02bWnUBFzLQ$yhCo/keSWZm4rO7TQ60+9WsnaQoXNWM7/I6TYGXlQgw', '管理员', NULL, NULL, NULL, '#276FF5', true, '2026-07-27 17:59:01.985745', NULL, '2026-07-29 17:18:03.520206', 1, 4, false);


--
-- Data for Name: t_scm_purchase_requisition; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_scm_purchase_requisition (id, number, subject, org_id, applicant_id, biz_date, required_date, reason, bill_status, version, create_time, update_time, create_user, update_user) VALUES (2081685365698519041, 'PR-ACCEPT-001', '架构纵向验收采购申请', 1, 1, '2026-07-27', NULL, NULL, 'B', 3, '2026-07-27 18:17:21.683574', '2026-07-27 18:22:01.617444', 1, 1);
INSERT INTO public.t_scm_purchase_requisition (id, number, subject, org_id, applicant_id, biz_date, required_date, reason, bill_status, version, create_time, update_time, create_user, update_user) VALUES (2082396658201919490, '123', '123', 1, 1, '2026-07-29', NULL, '12313', 'B', 2, '2026-07-29 17:23:47.032048', '2026-07-29 17:23:48.888725', 1, 1);


--
-- Data for Name: t_scm_purchase_requisition_entry; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_scm_purchase_requisition_entry (id, parent_id, material_name, specification, unit, quantity, required_date, remark, sort, create_time, update_time, create_user, update_user) VALUES (2081686539583537153, 2081685365698519041, '测试物料', NULL, '件', 1.000000, NULL, NULL, 1, '2026-07-27 18:22:01.560691', NULL, 1, NULL);
INSERT INTO public.t_scm_purchase_requisition_entry (id, parent_id, material_name, specification, unit, quantity, required_date, remark, sort, create_time, update_time, create_user, update_user) VALUES (2082396665957187586, 2082396658201919490, '11', '22', '22', 1.000000, '2026-07-29', '12313213', 1, '2026-07-29 17:23:48.880279', NULL, 1, NULL);


--
-- Data for Name: t_sys_cloud; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_cloud (id, name, number, seq, enabled, create_time, update_time, create_user, update_user, version) VALUES (4, '系统服务', 'sys', 99, true, '2026-04-22 13:47:23.710312', '2026-06-11 23:20:53.345438', NULL, 1, 0);
INSERT INTO public.t_sys_cloud (id, name, number, seq, enabled, create_time, update_time, create_user, update_user, version) VALUES (430000000000000001, '供应链', 'scm', 10, true, '2026-07-27 17:59:01.999094', '2026-07-27 17:59:01.999094', NULL, NULL, 0);


--
-- Data for Name: t_sys_app; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_app (id, name, number, icon, seq, description, cloud_id, enabled, create_time, update_time, icon_color, create_user, update_user, version) VALUES (430000000000000002, '采购管理', 'procurement', 'ShoppingCartOutlined', 1, '采购业务管理', 430000000000000001, true, '2026-07-27 17:59:01.999094', '2026-07-27 17:59:01.999094', '#1677ff', NULL, NULL, 0);
INSERT INTO public.t_sys_app (id, name, number, icon, seq, description, cloud_id, enabled, create_time, update_time, icon_color, create_user, update_user, version) VALUES (32, '任务调度', 'scheduler', 'ClockCircleOutlined', 3, '定时任务定义与执行实例管理', 4, true, '2026-07-30 23:40:19.392006', '2026-07-30 23:40:19.392006', '#1677ff', NULL, NULL, 0);
INSERT INTO public.t_sys_app (id, name, number, icon, seq, description, cloud_id, enabled, create_time, update_time, icon_color, create_user, update_user, version) VALUES (30, '系统监控', 'monitor', 'LineChartOutlined', 2, '系统监控', 4, true, '2026-04-22 13:47:23.710312', '2026-08-07 22:03:23.510525', '#ff0000', NULL, 1, 1);
INSERT INTO public.t_sys_app (id, name, number, icon, seq, description, cloud_id, enabled, create_time, update_time, icon_color, create_user, update_user, version) VALUES (31, '系统建模', 'base', 'AppstoreOutlined', 1, '云、应用、菜单、用户等基础数据', 4, true, '2026-04-22 18:06:56.092765', '2026-08-07 22:04:00.091407', '#1BA854', NULL, 1, 1);


--
-- Data for Name: t_sys_attachment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085907867161251841, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:56:04.482009', '2026-08-08 10:15:25.433752', 1, 1, 'E:\upload\asset\sys\base\ui-config\2f8600bf-38f1-43cc-afaf-7b69a3a6d9ff.jpg', 'DELETED', 'a4e051bf-a30e-45b3-88fa-f532314355e3', '2026-08-09 09:56:04.481487', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085911156166676482, '1.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 10:09:08.642485', '2026-08-08 10:18:07.891294', 1, 1, 'E:\upload\asset\sys\base\ui-config\786acde1-d6c7-4003-8512-5aba4d5b1e8d.jpg', 'DELETED', '83d1de1e-8ad5-4e6e-86a5-ff88462ef16d', '2026-08-09 10:09:08.641468', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085895933720440834, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:08:39.328595', '2026-08-08 09:23:29.774136', 1, 1, 'E:\upload\asset\sys\base\ui-config\d0d745c8-eecc-42e6-b2de-2d586f17214f.jpg', 'DELETED', '0494762f-aa29-4a18-9f3f-23148f31f505', '2026-08-09 09:08:39.325033', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085912664933322753, '1.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 10:15:08.361273', '2026-08-08 10:18:07.894802', 1, 1, 'E:\upload\asset\sys\base\ui-config\a05276c5-b51e-4c04-8330-9db51cc967fd.jpg', 'DELETED', 'cbf6a683-2f0b-423a-8deb-ec911fe7288d', '2026-08-09 10:15:08.360297', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085913621708566530, '1.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 10:18:56.473591', '2026-08-08 10:19:08.619891', 1, 1, 'E:\upload\asset\sys\base\ui-config\490af5ba-4ca6-45c3-8bbb-3d6da07a7441.jpg', 'DELETED', '6162ba31-fe2c-4dc8-842d-5b4e5ae25119', '2026-08-09 10:18:56.467564', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085913703174533121, '1.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 10:19:15.896166', '2026-08-08 10:19:31.083758', 1, 1, 'E:\upload\asset\sys\base\ui-config\5b8324bd-1797-4cf9-bb41-37f0ad916d3a.jpg', 'DELETED', '42cd676a-806b-4958-8101-ffc9401f4fa7', '2026-08-09 10:19:15.894123', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085896777551491073, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:12:00.513065', '2026-08-08 09:49:36.492271', 1, 1, 'E:\upload\asset\sys\base\ui-config\851c594c-7aeb-4859-acbe-085dafe97c7b.jpg', 'DELETED', '8d19e23f-052f-44c1-8371-04382ece223c', '2026-08-09 09:12:00.511043', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085906429215428610, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:50:21.649882', '2026-08-08 09:51:08.49702', 1, 1, 'E:\upload\asset\sys\base\ui-config\b404d04e-8e69-4038-9368-879867d1129d.jpg', 'DELETED', '9ca19b46-4294-4513-b6e8-9f141e8ee47a', '2026-08-09 09:50:21.648843', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085906586514411521, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:50:59.152762', '2026-08-08 09:51:37.619487', 1, 1, 'E:\upload\asset\sys\base\ui-config\425e2307-4f0e-4dd3-baac-80c62bb0999e.jpg', 'DELETED', '16e65e8e-a1af-449d-8d30-c9e85b654e99', '2026-08-09 09:50:59.151663', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085906734309101570, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:51:34.389798', '2026-08-08 09:56:06.031779', 1, 1, 'E:\upload\asset\sys\base\ui-config\83c848b5-7b76-4cc4-85c6-8b79ada92a1d.jpg', 'DELETED', '9936bc11-f32e-437e-9844-52557cd6244e', '2026-08-09 09:51:34.38864', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085906231093284866, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:49:34.414318', '2026-08-08 09:59:12.806459', 1, 1, 'E:\upload\asset\sys\base\ui-config\e3601dea-fec5-4741-af8e-b022ec816d82.jpg', 'DELETED', '1a624637-28af-4752-864e-4e7cb16c6a83', '2026-08-09 09:49:34.411346', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');
INSERT INTO public.t_sys_attachment (id, original_name, file_size, mime_type, file_ext, storage_type, create_time, update_time, create_user, update_user, object_key, status, upload_session_id, expires_at, sha256) VALUES (2085908651517071361, '微信图片_20240529205314.jpg', 58756, 'image/jpeg', '.jpg', 'LOCAL', '2026-08-08 09:59:11.48772', '2026-08-08 10:09:15.040981', 1, 1, 'E:\upload\asset\sys\base\ui-config\1679fb3a-66c4-4501-be2f-ccff44309717.jpg', 'DELETED', '6b9d7516-4bb7-4bf5-b54f-db93c5f55803', '2026-08-09 09:59:11.486701', '68b5f432531a47d41a832a3f027eed6e20091708ddaf95eafb03ae31227110aa');


--
-- Data for Name: t_sys_attachment_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_attachment_config (id, max_upload_bytes, allowed_extensions, allowed_mime_types, temp_expire_hours, version, create_time, update_time, create_user, update_user) VALUES (420000000000001101, 20971520, 'pdf,png,jpg,jpeg,gif,webp,doc,docx,xls,xlsx,ppt,pptx,txt', 'application/pdf,image/png,image/jpeg,image/gif,image/webp,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,text/plain', 24, 0, '2026-08-06 23:55:26.160595', NULL, NULL, NULL);


--
-- Data for Name: t_sys_basic_data_category; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_basic_data_item; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_biz_attachment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_file_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_file_config (id, storage_type, local_dir, ftp_host, ftp_port, ftp_username, ftp_dir, ftp_passive_mode, create_time, update_time, create_user, update_user, ftp_password_cipher, version, s3_endpoint, s3_region, s3_bucket, s3_access_key, s3_secret_key_cipher, s3_path_style) VALUES (2082478248768778241, 'LOCAL', 'E:/upload/', NULL, 21, NULL, NULL, true, '2026-07-29 22:47:59.740268', '2026-08-07 16:12:48.110574', 1, 1, NULL, 2, NULL, NULL, NULL, NULL, NULL, true);


--
-- Data for Name: t_sys_job; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_job (id, job_name, job_group, description, job_class_name, cron_expression, job_data, status, remark, create_time, update_time, create_user, update_user, number, is_system, version, mutex_key) VALUES (2082857218823630850, '测试', 'DEFAULT', NULL, 'sm.domain.sys.scheduler.job.CleanTempFileJob', '0 0/30 * * * ?', '', 'PAUSED', '', '2026-07-30 23:53:53.241535', '2026-07-31 11:33:23.179235', 1, 1, 'test', false, 7, NULL);
INSERT INTO public.t_sys_job (id, job_name, job_group, description, job_class_name, cron_expression, job_data, status, remark, create_time, update_time, create_user, update_user, number, is_system, version, mutex_key) VALUES (440000000000000001, '系统日志分区转储', 'SYSTEM', NULL, 'sm.domain.sys.scheduler.job.ArchiveSystemLogJob', '0 10 2 * * ?', '{"loginLogHotDays":180,"operateLogHotDays":180,"sqlLogHotDays":180,"scriptLogHotDays":180,"jobLogHotDays":90,"maxPartitionsPerRun":12}', 'PAUSED', '将超过在线保留期的完整月分区转入历史父表', '2026-08-10 16:19:04.823455', NULL, NULL, NULL, 'SYSTEM_LOG_ARCHIVE', true, 0, 'system-log-lifecycle');
INSERT INTO public.t_sys_job (id, job_name, job_group, description, job_class_name, cron_expression, job_data, status, remark, create_time, update_time, create_user, update_user, number, is_system, version, mutex_key) VALUES (440000000000000002, '系统日志历史淘汰', 'SYSTEM', NULL, 'sm.domain.sys.scheduler.job.PurgeSystemLogHistoryJob', '0 40 2 * * ?', '{"loginLogRetentionDays":1095,"operateLogRetentionDays":1095,"sqlLogRetentionDays":730,"scriptLogRetentionDays":730,"jobLogRetentionDays":365,"maxPartitionsPerRun":12}', 'PAUSED', '删除超过历史保留期的完整月分区', '2026-08-10 16:19:04.823455', NULL, NULL, NULL, 'SYSTEM_LOG_HISTORY_PURGE', true, 0, 'system-log-lifecycle');


--
-- Data for Name: t_sys_permission; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10031, '云管理-查询', 'sys:base:cloud:listPage', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10032, '云管理-详情', 'sys:base:cloud:detail', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10033, '云管理-保存', 'sys:base:cloud:save', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10034, '云管理-删除', 'sys:base:cloud:delete', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10035, '应用管理-查询', 'sys:base:app:listPage', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10036, '应用管理-详情', 'sys:base:app:detail', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10037, '应用管理-保存', 'sys:base:app:save', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10038, '应用管理-删除', 'sys:base:app:delete', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10014, '权限管理-查询', 'sys:base:permission:listPage', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10012, '用户管理-查询', 'sys:base:user:listPage', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10015, '角色管理-查询', 'sys:base:role:listPage', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10016, '权限管理-保存', 'sys:base:permission:save', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10017, '权限管理-详情', 'sys:base:permission:detail', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10013, '菜单管理-查询', 'sys:base:menu:listPage', 31, NULL, '2026-04-27 12:17:57.584541', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10039, '角色管理-详情', 'sys:base:role:detail', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10040, '角色管理-保存', 'sys:base:role:save', 31, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10041, '权限管理-选择', 'sys:base:permission:select', 31, '2026-04-27 13:45:08.495725', NULL, 1, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10042, '权限管理-删除', 'sys:base:permission:delete', 31, '2026-04-27 13:45:51.39581', NULL, 1, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (406250201727746048, '菜单管理-详情', 'sys:base:menu:detail', 31, '2026-04-27 13:54:15.855314', NULL, 1, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10030, '系统建模-应用入口', 'sys:base:access', 31, NULL, '2026-04-27 14:00:32.890395', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (406254838245605376, '菜单管理-保存', 'sys:base:menu:save', 31, '2026-04-27 14:12:41.287791', NULL, 1, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (406259661691011072, '菜单管理-选择', 'sys:base:menu:select', 31, '2026-04-27 14:31:51.286645', NULL, 1, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10022, '登录日志-查询', 'sys:log:login:listPage', 30, NULL, '2026-04-27 15:38:27.280076', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10023, '操作日志-查询', 'sys:log:operate:listPage', 30, NULL, '2026-04-27 15:38:38.913184', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (10020, '日志应用入口', 'sys:log:access', 30, NULL, '2026-05-13 18:13:36.35951', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413172783453237248, '界面配置列表', 'sys:base:ui-config:listPage', 31, '2026-05-16 16:22:07.956473', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413172783499374592, '界面配置详情', 'sys:base:ui-config:detail', 31, '2026-05-16 16:22:07.965499', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413172783507763200, '界面配置保存', 'sys:base:ui-config:save', 31, '2026-05-16 16:22:07.968478', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413196675722964992, '文件配置列表', 'sys:base:file-config:listPage', 31, '2026-05-16 17:57:04.317767', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413196675756519424, '文件配置详情', 'sys:base:file-config:detail', 31, '2026-05-16 17:57:04.323767', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413196675764908032, '文件配置保存', 'sys:base:file-config:save', 31, '2026-05-16 17:57:04.325767', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50050, '系统参数分类', 'sys:base:param:category', 31, '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50051, '系统参数列表', 'sys:base:param:listPage', 31, '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50052, '系统参数详情', 'sys:base:param:detail', 31, '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50053, '系统参数编辑', 'sys:base:param:save', 31, '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50054, '系统参数删除', 'sys:base:param:delete', 31, '2026-05-17 01:13:34.738535', '2026-05-17 01:13:34.738535', NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50070, '缓存管理列表', 'sys:monitor:cache:listPage', 30, '2026-05-17 20:09:21.311371', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (411644663060602880, '基础数据管理-删除', 'sys:base:basic-data:delete', 31, '2026-05-12 11:09:55.661809', '2026-05-12 11:15:10.76583', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (411644663027048448, '基础数据管理-详情', 'sys:base:basic-data:detail', 31, '2026-05-12 11:09:55.653679', '2026-05-12 11:15:21.394506', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (411644662943162368, '基础数据管理-列表', 'sys:base:basic-data:listPage', 31, '2026-05-12 11:09:55.638553', '2026-05-12 11:15:30.967268', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (411644663043825664, '基础数据管理-保存', 'sys:base:basic-data:save', 31, '2026-05-12 11:09:55.65768', '2026-05-12 11:15:44.892574', NULL, 1, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (419000000000000006, '脚本控制台', 'sys:monitor:script', 30, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001001, '用户管理-启用', 'sys:base:user:enable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001002, '用户管理-禁用', 'sys:base:user:disable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001003, '云管理-启用', 'sys:base:cloud:enable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001004, '云管理-禁用', 'sys:base:cloud:disable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001005, '应用管理-启用', 'sys:base:app:enable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001006, '应用管理-禁用', 'sys:base:app:disable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001007, '菜单管理-启用', 'sys:base:menu:enable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001008, '菜单管理-禁用', 'sys:base:menu:disable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001009, '基础数据管理-启用', 'sys:base:basic-data:enable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (50071, '应用缓存-清理', 'sys:monitor:cache:clear', 30, '2026-05-17 20:09:21.311371', '2026-08-03 21:22:35.436359', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413501707400000001, 'SQL控制台-执行', 'sys:monitor:sql:execute', 30, '2026-05-18 00:27:43.408601', '2026-08-04 22:10:07.527334', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413501707400000003, 'SQL执行历史-列表', 'sys:monitor:sql:log:listPage', 30, '2026-05-18 00:43:01.026314', '2026-08-04 22:10:07.527334', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (419000000000000001, '脚本控制台-执行', 'sys:monitor:script:execute', 30, NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (419000000000000002, '脚本管理-列表', 'sys:monitor:script:listPage', 30, NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (419000000000000003, '脚本管理-详情', 'sys:monitor:script:detail', 30, NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (419000000000000004, '脚本管理-保存', 'sys:monitor:script:save', 30, NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (419000000000000005, '脚本管理-删除', 'sys:monitor:script:delete', 30, NULL, '2026-08-05 00:25:49.513377', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413501707269836800, '运行监控-查看', 'sys:monitor:node:view', 30, '2026-05-17 14:09:09.506432', '2026-08-10 10:36:56.930081', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413501707320168448, '线程诊断-采集', 'sys:monitor:thread:collect', 30, '2026-05-17 14:09:09.515591', '2026-08-10 12:34:28.537208', NULL, NULL, 2);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001010, '基础数据管理-禁用', 'sys:base:basic-data:disable', 31, '2026-07-27 17:59:01.958211', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001011, '角色管理-分配权限', 'sys:base:role:assignPermissions', 31, '2026-07-27 17:59:01.975551', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001012, '用户管理-分配角色', 'sys:base:user:assignRoles', 31, '2026-07-27 17:59:01.975551', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (430000000000000010, '采购申请', 'scm:procurement:purchase-requisition', 430000000000000002, '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (430000000000000011, '采购申请-列表', 'scm:procurement:purchase-requisition:listPage', 430000000000000002, '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (430000000000000012, '采购申请-详情', 'scm:procurement:purchase-requisition:detail', 430000000000000002, '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (430000000000000013, '采购申请-保存', 'scm:procurement:purchase-requisition:save', 430000000000000002, '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (430000000000000014, '采购申请-提交', 'scm:procurement:purchase-requisition:submit', 430000000000000002, '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (430000000000000015, '采购申请-删除', 'scm:procurement:purchase-requisition:delete', 430000000000000002, '2026-07-27 17:59:01.999094', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001013, '用户管理-重置密码', 'sys:base:user:resetPassword', 31, '2026-07-29 17:34:17.647656', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001014, '登录日志-详情', 'sys:log:login:detail', 30, '2026-07-30 18:45:20.440561', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001015, '操作日志-详情', 'sys:log:operate:detail', 30, '2026-07-30 18:45:20.440561', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413260828487667712, '定时任务列表', 'sys:scheduler:job:listPage', 32, '2026-05-16 22:11:59.528179', '2026-07-30 23:40:19.392006', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413260828529610752, '定时任务详情', 'sys:scheduler:job:detail', 32, '2026-05-16 22:11:59.536189', '2026-07-30 23:40:19.392006', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413260828537999360, '定时任务编辑', 'sys:scheduler:job:save', 32, '2026-05-16 22:11:59.538188', '2026-07-30 23:40:19.392006', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413260828546387968, '定时任务删除', 'sys:scheduler:job:delete', 32, '2026-05-16 22:11:59.540189', '2026-07-30 23:40:19.392006', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (413260828550582272, '执行实例列表', 'sys:scheduler:execution:listPage', 32, '2026-05-16 22:11:59.541187', '2026-07-30 23:40:19.392006', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (5001, '任务调度分类', 'sys:scheduler:category', 32, '2026-05-16 22:33:07.430001', '2026-07-30 23:40:19.392006', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001016, '执行实例-详情', 'sys:scheduler:execution:detail', 32, '2026-07-30 23:40:19.392006', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (421000000000000001, '应用缓存-全部清理', 'sys:monitor:cache:clearAll', 30, '2026-08-03 21:22:35.436359', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (421000000000000005, '缓存管理-查看值', 'sys:monitor:cache:value', 30, '2026-08-03 21:59:34.573686', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (421000000000000006, '缓存管理-删除', 'sys:monitor:cache:delete', 30, '2026-08-03 21:59:34.573686', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (425000000000000001, 'SQL执行历史-详情', 'sys:monitor:sql:log:detail', 30, '2026-08-04 22:10:07.527334', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (426000000000000001, '脚本执行历史-列表', 'sys:monitor:script:log:listPage', 30, '2026-08-05 00:25:49.513377', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (426000000000000002, '脚本执行历史-详情', 'sys:monitor:script:log:detail', 30, '2026-08-05 00:25:49.513377', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001017, '云管理-选择', 'sys:base:cloud:select', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001018, '菜单管理-删除', 'sys:base:menu:delete', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001019, '角色管理-选择', 'sys:base:role:select', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001020, '角色管理-删除', 'sys:base:role:delete', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001021, '用户管理-详情', 'sys:base:user:detail', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001022, '用户管理-保存', 'sys:base:user:save', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001023, '用户管理-删除', 'sys:base:user:delete', 31, '2026-08-06 22:44:05.351384', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001102, '附件配置-查看', 'sys:base:attachment-config:detail', 31, '2026-08-06 23:55:26.160595', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (420000000000001103, '附件配置-保存', 'sys:base:attachment-config:save', 31, '2026-08-06 23:55:26.160595', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (438000000000000001, '线程诊断-访问', 'sys:monitor:thread:access', 30, '2026-08-10 10:36:56.930081', '2026-08-10 12:34:28.537208', NULL, NULL, 1);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (441000000000000001, '慢SQL监控-访问', 'sys:monitor:slow-sql:access', 30, '2026-08-10 17:22:51.566885', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (441000000000000002, '慢SQL监控-配置', 'sys:monitor:slow-sql:config', 30, '2026-08-10 17:22:51.566885', NULL, NULL, NULL, 0);
INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, update_time, create_user, update_user, version) VALUES (441000000000000003, '慢SQL监控-清空', 'sys:monitor:slow-sql:clear', 30, '2026-08-10 17:22:51.566885', NULL, NULL, NULL, 0);


--
-- Data for Name: t_sys_menu; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (441000000000000010, 'slow_sql_monitoring', '慢 SQL 监控', 1, 413501707332751360, 30, 441000000000000001, '/sys/monitor/slow-sql', 'sys/monitor/slow-sql', 'DatabaseOutlined', '查看指定应用实例的 Druid SQL 内存聚合统计', 20, true, '2026-08-10 17:22:51.574039', '2026-08-10 17:22:51.574039', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (3101, 'base_management', '基础数据', 0, 0, 31, 10030, NULL, NULL, 'LaptopOutlined', '分组', 1, true, '2026-04-22 18:06:56.092765', '2026-08-07 21:59:39.742514', NULL, 1, 1);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (3002, 'login_log', '登录日志', 1, 3000, 30, 10022, '/sys/monitor/login-log', 'sys/monitor/login-log', 'FileTextOutlined', '登录日志', 1, true, '2026-04-22 13:47:23.710312', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413172783545511936, 'ui_config', '界面配置', 1, 413172783532929024, 31, 413172783499374592, '/sys/base/ui-config', 'sys/base/ui-config', 'SettingOutlined', NULL, 10, true, '2026-05-16 16:22:07.976623', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (2104, 'permission', '权限管理', 1, 3101, 31, 10014, '/sys/base/permission', 'sys/base/permission', 'SettingOutlined', '权限', 5, true, '2026-04-14 13:59:27.544725', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (50061, 'sys_param', '系统参数', 1, 50060, 31, 50051, '/sys/base/sys-param', 'sys/base/sys-param', 'SettingOutlined', NULL, 1, true, '2026-05-17 01:13:51.334518', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (419000000000000011, 'script_console', '脚本控制台', 1, 419000000000000010, 30, 419000000000000001, '/sys/monitor/script-console', 'sys/monitor/script-console', NULL, '执行受控的服务端 JavaScript 运维脚本', 1, true, '2026-05-19 14:17:47.759591', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (3000, 'log_monitoring', '日志监控', 0, 0, 30, 10020, NULL, NULL, 'FileTextOutlined', '分组', 1, true, '2026-04-22 13:47:23.710312', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (2102, 'user', '用户管理', 1, 3101, 31, 10012, '/sys/base/user', 'sys/base/user', 'UserOutlined', '用户', 1, true, '2026-04-14 13:59:27.544725', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (50081, 'cache_status', '缓存状态', 1, 50080, 30, 50070, '/sys/monitor/cache-status', 'sys/monitor/cache-status', 'LinkOutlined', 'Redis 运行状态与 JetCache 实时统计', 1, true, '2026-05-17 20:09:21.311371', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707400000002, 'sql_console', 'SQL 执行', 1, 413501707410000001, 30, 413501707400000001, '/sys/monitor/sql-console', 'sys/monitor/sql-console', 'ConsoleSqlOutlined', '执行 PostgreSQL 查询、单条命令或批量 INSERT', 1, true, '2026-05-18 00:28:23.884474', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (5001, 'scheduler_management', '任务调度', 0, 0, 32, 5001, NULL, NULL, 'ClockCircleOutlined', NULL, 2, true, '2026-05-16 22:33:07.430001', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (3102, 'cloud', '云管理', 1, 3101, 31, 10031, '/sys/base/cloud', 'sys/base/cloud', 'AppstoreOutlined', '云管理', 2, true, '2026-04-22 18:06:56.092765', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707400000004, 'sql_execution_log', '执行历史', 1, 413501707410000001, 30, 413501707400000003, '/sys/monitor/sql-log', 'sys/monitor/sql-log', 'FileTextOutlined', '查看 SQL 控制台执行审计', 2, true, '2026-05-18 00:43:01.026314', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (3003, 'operation_log', '操作日志', 1, 3000, 30, 10023, '/sys/monitor/operate-log', 'sys/monitor/operate-log', 'SearchOutlined', '操作日志', 2, true, '2026-04-22 13:47:23.710312', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (50080, 'cache_monitoring', '缓存监控', 0, 0, 30, 50070, NULL, NULL, 'SyncOutlined', NULL, 20, true, '2026-05-17 20:09:21.311371', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (3103, 'app', '应用管理', 1, 3101, 31, 10035, '/sys/base/app', 'sys/base/app', 'AppstoreOutlined', '应用管理', 3, true, '2026-04-22 18:06:56.092765', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (2103, 'menu', '菜单管理', 1, 3101, 31, 10013, '/sys/base/menu', 'sys/base/menu', 'MenuOutlined', '菜单', 4, true, '2026-04-14 13:59:27.544725', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (2105, 'role', '角色管理', 1, 3101, 31, 10015, '/sys/base/role', 'sys/base/role', 'IdcardOutlined', '角色', 6, true, '2026-04-21 10:54:03.230143', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413196675798462464, 'file_config', '文件配置', 1, 413196675785879552, 31, 413196675756519424, '/sys/base/file-config', 'sys/base/file-config', 'FileOutlined', NULL, 10, true, '2026-05-16 17:57:04.333267', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413260828563165184, 'job', '定时任务', 1, 5001, 32, 413260828487667712, '/sys/scheduler/job', 'sys/scheduler/job', 'ClockCircleOutlined', NULL, 30, true, '2026-05-16 22:11:59.544189', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707332751360, 'service_monitoring', '服务监控', 0, 0, 30, 413501707269836800, NULL, NULL, 'DashboardOutlined', NULL, 30, true, '2026-05-17 14:09:09.518595', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413260828571553792, 'execution', '执行实例', 1, 5001, 32, 413260828550582272, '/sys/scheduler/execution', 'sys/scheduler/execution', 'HistoryOutlined', NULL, 31, true, '2026-05-16 22:11:59.546207', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707410000001, 'sql_console_management', 'SQL 控制台', 0, 0, 30, 413501707400000001, NULL, NULL, 'ConsoleSqlOutlined', NULL, 35, true, '2026-05-18 00:42:29.47291', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (419000000000000010, 'script_management', '脚本控制台', 0, 0, 30, 419000000000000006, NULL, NULL, 'CodeOutlined', NULL, 40, true, '2026-05-19 14:17:47.757984', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (411644663089963008, 'basic_data', '基础数据管理', 1, 3101, 31, 411644662943162368, 'sys/base/basic-data', 'sys/base/basic-data', 'ApartmentOutlined', NULL, 70, true, '2026-05-12 11:09:55.668845', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413172783532929024, 'ui_configuration', '界面配置', 0, 0, 31, 413172783453237248, NULL, NULL, 'SettingOutlined', NULL, 80, true, '2026-05-16 16:22:07.973646', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413196675785879552, 'file_configuration', '文件配置', 0, 0, 31, 413196675722964992, NULL, NULL, 'FileOutlined', NULL, 90, true, '2026-05-16 17:57:04.331269', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (50060, 'system_parameters', '系统参数', 0, 0, 31, 50050, NULL, NULL, 'SettingOutlined', NULL, 110, true, '2026-05-17 01:13:51.334518', '2026-08-07 17:52:48.405183', NULL, 1, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (430000000000000020, 'purchase_requisition', '采购申请', 1, 430000000000000019, 430000000000000002, 430000000000000010, '/scm/procurement/purchase-requisition', 'scm/procurement/purchase-requisition', 'FileAddOutlined', '采购申请单', 1, true, '2026-07-27 17:59:01.999094', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (430000000000000019, 'procurement_business', '采购业务', 0, 0, 430000000000000002, 430000000000000010, NULL, NULL, 'ShoppingOutlined', '采购业务', 1, true, '2026-07-27 17:59:02.041045', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (421000000000000010, 'cache', '缓存管理', 1, 50080, 30, 50070, '/sys/monitor/cache-management', 'sys/monitor/cache-management', 'DatabaseOutlined', '统一查看和操作本地与 Redis 缓存', 2, true, '2026-08-03 21:22:35.436359', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (426000000000000011, 'script', '脚本管理', 1, 419000000000000010, 30, 419000000000000002, '/sys/monitor/script-manage', 'sys/monitor/script-manage', NULL, '维护可复用的运维脚本', 2, true, '2026-08-05 00:25:49.513377', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (426000000000000012, 'script_execution_log', '执行历史', 1, 419000000000000010, 30, 426000000000000001, '/sys/monitor/script-log', 'sys/monitor/script-log', NULL, '查看脚本控制台执行审计', 3, true, '2026-08-05 00:25:49.513377', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (420000000000001104, 'attachment_config', '附件配置', 1, 413196675785879552, 31, 420000000000001102, '/sys/base/attachment-config', 'sys/base/attachment-config', 'PaperClipOutlined', '统一管理附件上传限制和临时附件有效期', 20, true, '2026-08-06 23:55:26.160595', '2026-08-07 17:52:48.405183', NULL, NULL, 0);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707345334272, 'node_monitoring', '运行监控', 1, 413501707332751360, 30, 413501707269836800, '/sys/monitor/node', 'sys/monitor/node', 'DashboardOutlined', '选择在线应用实例并查看运行快照', 10, true, '2026-05-17 14:09:09.521627', '2026-08-10 12:34:28.537208', NULL, NULL, 2);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707391471616, 'thread_diagnostic', '线程诊断', 1, 413501707370500096, 30, 438000000000000001, '/sys/monitor/thread', 'sys/monitor/thread', 'ToolOutlined', '选择在线实例并查看线程、堆栈、热点和死锁信息', 10, true, '2026-05-17 14:09:09.53259', '2026-08-10 12:34:28.537208', NULL, NULL, 2);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time, update_time, create_user, update_user, version) VALUES (413501707370500096, 'diagnostic_tools', '诊断工具', 0, 0, 30, 438000000000000001, NULL, NULL, 'ToolOutlined', '面向超级管理员的生产问题诊断能力', 40, true, '2026-05-17 14:09:09.527589', '2026-08-10 12:34:28.537208', NULL, 1, 2);


--
-- Data for Name: t_sys_param; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_param (id, number, name, value, remark, create_time, update_time, create_user, update_user, is_system, version, app_id) VALUES (425000000000000010, 'SQL_CONSOLE_MAX_ROWS', 'SQL 控制台最大返回行数', '1000', '允许范围 1～5000；超过限制的查询结果会被截断', '2026-08-04 22:10:07.527334', NULL, NULL, NULL, true, 0, 30);
INSERT INTO public.t_sys_param (id, number, name, value, remark, create_time, update_time, create_user, update_user, is_system, version, app_id) VALUES (426000000000000010, 'SCRIPT_CONSOLE_TIMEOUT_SECONDS', '脚本控制台超时秒数', '30', '允许范围 1～300 秒；超时将取消 JavaScript 并回滚原子事务', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, true, 0, 30);
INSERT INTO public.t_sys_param (id, number, name, value, remark, create_time, update_time, create_user, update_user, is_system, version, app_id) VALUES (426000000000000011, 'SCRIPT_CONSOLE_MAX_SOURCE_LENGTH', '脚本控制台最大源码长度', '100000', '允许范围 1000～1000000 字符', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, true, 0, 30);
INSERT INTO public.t_sys_param (id, number, name, value, remark, create_time, update_time, create_user, update_user, is_system, version, app_id) VALUES (426000000000000012, 'SCRIPT_CONSOLE_MAX_OUTPUT_LENGTH', '脚本控制台最大输出长度', '100000', '允许范围 1000～1000000 字符；超过限制的输出将被截断', '2026-08-05 00:25:49.513377', NULL, NULL, NULL, true, 0, 30);


--
-- Data for Name: t_sys_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_role (id, name, number, create_time, update_time, create_user, update_user, version) VALUES (1, '系统管理员', 'admin', NULL, '2026-04-27 15:39:34.171162', NULL, 1, 0);


--
-- Data for Name: t_sys_role_perms; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477120, 1, 10020, '2026-04-27 15:39:34.19102', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477121, 1, 10031, '2026-04-27 15:39:34.192028', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477122, 1, 10032, '2026-04-27 15:39:34.192028', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477123, 1, 10033, '2026-04-27 15:39:34.192028', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477124, 1, 10034, '2026-04-27 15:39:34.192028', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477125, 1, 10035, '2026-04-27 15:39:34.193162', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477126, 1, 10036, '2026-04-27 15:39:34.193162', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477127, 1, 10037, '2026-04-27 15:39:34.193994', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477128, 1, 10038, '2026-04-27 15:39:34.194304', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477129, 1, 10014, '2026-04-27 15:39:34.194304', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477130, 1, 10012, '2026-04-27 15:39:34.194304', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477131, 1, 10015, '2026-04-27 15:39:34.195304', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477132, 1, 10016, '2026-04-27 15:39:34.195653', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477133, 1, 10017, '2026-04-27 15:39:34.195946', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702745477134, 1, 10013, '2026-04-27 15:39:34.196238', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671424, 1, 10039, '2026-04-27 15:39:34.19652', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671425, 1, 10040, '2026-04-27 15:39:34.19652', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671426, 1, 10041, '2026-04-27 15:39:34.197102', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671427, 1, 10042, '2026-04-27 15:39:34.197396', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671429, 1, 406250201727746048, '2026-04-27 15:39:34.197999', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671431, 1, 10030, '2026-04-27 15:39:34.1983', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671432, 1, 406254838245605376, '2026-04-27 15:39:34.198917', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671433, 1, 406259661691011072, '2026-04-27 15:39:34.19923', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671434, 1, 10022, '2026-04-27 15:39:34.199547', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (406276702749671435, 1, 10023, '2026-04-27 15:39:34.199547', NULL, 1, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000020, 1, 419000000000000001, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000021, 1, 419000000000000002, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000022, 1, 419000000000000003, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000023, 1, 419000000000000004, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000024, 1, 419000000000000005, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000025, 1, 419000000000000006, NULL, NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000026, 1, 420000000000001014, '2026-07-30 18:45:20.440561', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000027, 1, 420000000000001015, '2026-07-30 18:45:20.440561', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000028, 1, 5001, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000029, 1, 413260828487667712, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000030, 1, 413260828529610752, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000031, 1, 413260828537999360, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000032, 1, 413260828546387968, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000033, 1, 413260828550582272, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000034, 1, 420000000000001016, '2026-07-30 23:43:20.377026', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000038, 1, 421000000000000005, '2026-08-03 21:59:34.573686', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000039, 1, 421000000000000006, '2026-08-03 21:59:34.573686', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000040, 1, 413501707400000001, '2026-08-04 22:10:07.527334', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000041, 1, 413501707400000003, '2026-08-04 22:10:07.527334', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000042, 1, 425000000000000001, '2026-08-04 22:10:07.527334', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000043, 1, 426000000000000001, '2026-08-05 00:25:49.513377', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000044, 1, 426000000000000002, '2026-08-05 00:25:49.513377', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000045, 1, 420000000000001017, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000046, 1, 420000000000001018, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000047, 1, 420000000000001019, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000048, 1, 420000000000001020, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000049, 1, 420000000000001021, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000050, 1, 420000000000001022, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000051, 1, 420000000000001023, '2026-08-06 22:44:05.351384', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000052, 1, 420000000000001102, '2026-08-06 23:55:26.160595', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000053, 1, 420000000000001103, '2026-08-06 23:55:26.160595', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000054, 1, 441000000000000001, '2026-08-10 17:23:29.608555', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000055, 1, 441000000000000002, '2026-08-10 17:23:29.608555', NULL, NULL, NULL);
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time, update_time, create_user, update_user) VALUES (419000000000000056, 1, 441000000000000003, '2026-08-10 17:23:29.608555', NULL, NULL, NULL);


--
-- Data for Name: t_sys_script; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_sys_ui_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_sys_ui_config (id, page_title, login_banner, login_logo, system_name, header_logo, create_time, update_time, create_user, update_user, version, login_banner_attachment_id, login_logo_attachment_id, header_logo_attachment_id) VALUES (2085385490455228417, 'Smart Manage', NULL, NULL, 'Smart Manage', NULL, '2026-08-06 23:20:20.177898', '2026-08-08 10:20:31.97883', 1, 1, 37, NULL, NULL, NULL);


--
-- Data for Name: t_sys_user_role; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- PostgreSQL database dump complete
--
