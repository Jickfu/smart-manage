-- 菜单编码统一使用小写 snake_case；页面采用所属业务单据编码，分组采用语义化编码。
WITH standardized_menu_numbers (id, number) AS (
    VALUES
        (3002, 'login_log'),
        (413172783545511936, 'ui_config'),
        (2104, 'permission'),
        (50061, 'sys_param'),
        (419000000000000011, 'script_console'),
        (3101, 'base_management'),
        (2102, 'user'),
        (50081, 'cache_status'),
        (413501707400000002, 'sql_console'),
        (3000, 'log_monitoring'),
        (5001, 'scheduler_management'),
        (3102, 'cloud'),
        (413501707400000004, 'sql_execution_log'),
        (3003, 'operation_log'),
        (50080, 'cache_monitoring'),
        (3103, 'app'),
        (2103, 'menu'),
        (2105, 'role'),
        (413196675798462464, 'file_config'),
        (413501707391471616, 'arthas'),
        (413501707345334272, 'node_monitoring'),
        (413501707362111488, 'datasource_monitoring'),
        (413260828563165184, 'job'),
        (413501707332751360, 'service_monitoring'),
        (413260828571553792, 'execution'),
        (413501707410000001, 'sql_console_management'),
        (419000000000000010, 'script_management'),
        (413501707370500096, 'diagnostic_tools'),
        (411644663089963008, 'basic_data'),
        (413172783532929024, 'ui_configuration'),
        (413196675785879552, 'file_configuration'),
        (50060, 'system_parameters'),
        (430000000000000020, 'purchase_requisition'),
        (430000000000000019, 'procurement_business'),
        (421000000000000010, 'cache'),
        (426000000000000011, 'script'),
        (426000000000000012, 'script_execution_log'),
        (420000000000001104, 'attachment_config')
)
UPDATE public.t_sys_menu menu
SET number = standardized_menu_numbers.number,
    update_time = CURRENT_TIMESTAMP
FROM standardized_menu_numbers
WHERE menu.id = standardized_menu_numbers.id;

-- 历史菜单层级 2/3 收敛为新的枚举值 0/1，和 MenuLevelEnum 保持一致。
UPDATE public.t_sys_menu
SET level = CASE level
    WHEN 2 THEN 0
    WHEN 3 THEN 1
    ELSE level
END,
    update_time = CURRENT_TIMESTAMP
WHERE level IN (2, 3);

-- 不猜测自定义菜单的业务语义；若遗留编码不合规，阻止迁移并要求显式确认。
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.t_sys_menu
        WHERE number IS NULL
           OR number !~ '^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$'
    ) THEN
        RAISE EXCEPTION '存在不符合小写 snake_case 规范的菜单编码，请先显式确定其业务编码';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.t_sys_menu
        WHERE level NOT IN (0, 1)
    ) THEN
        RAISE EXCEPTION '存在无法转换的菜单层级，仅允许 0（分组）或 1（页面）';
    END IF;
END
$$;
