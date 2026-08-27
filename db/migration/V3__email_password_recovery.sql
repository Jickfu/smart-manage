ALTER TABLE public.t_sys_user
    ADD COLUMN email_verified_at timestamp without time zone;

COMMENT ON COLUMN public.t_sys_user.email_verified_at IS '邮箱验证时间';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.t_sys_user
        WHERE NULLIF(BTRIM(email), '') IS NOT NULL
        GROUP BY LOWER(BTRIM(email)) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION '用户邮箱存在重复数据，无法建立唯一索引，请先人工处理';
    END IF;
    IF EXISTS (
        SELECT 1 FROM public.t_sys_user
        WHERE NULLIF(BTRIM(phone), '') IS NOT NULL
        GROUP BY BTRIM(phone) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION '用户手机号存在重复数据，无法建立唯一索引，请先人工处理';
    END IF;
END $$;

UPDATE public.t_sys_user SET email = LOWER(BTRIM(email)) WHERE email IS NOT NULL;
UPDATE public.t_sys_user SET phone = BTRIM(phone) WHERE phone IS NOT NULL;

CREATE UNIQUE INDEX uk_sys_user_email_normalized
    ON public.t_sys_user (LOWER(BTRIM(email)))
    WHERE NULLIF(BTRIM(email), '') IS NOT NULL;
CREATE UNIQUE INDEX uk_sys_user_phone_normalized
    ON public.t_sys_user (BTRIM(phone))
    WHERE NULLIF(BTRIM(phone), '') IS NOT NULL;

ALTER TABLE public.t_sys_email_task
    ADD COLUMN sensitive_content boolean DEFAULT false NOT NULL,
    ADD COLUMN html_body_cipher text,
    ADD COLUMN text_body_cipher text;

COMMENT ON COLUMN public.t_sys_email_task.sensitive_content IS '是否为敏感正文';
COMMENT ON COLUMN public.t_sys_email_task.html_body_cipher IS '敏感HTML正文密文';
COMMENT ON COLUMN public.t_sys_email_task.text_body_cipher IS '敏感纯文本正文密文';

INSERT INTO public.t_sys_param
    (id, number, name, value, description, create_time, is_system, version, feature_id)
VALUES
    (480000000000000012, 'PASSWORD_EMAIL_CODE_EXPIRE_MINUTES', '邮箱改密验证码有效分钟数', '10',
     '必须为正整数；建议 5～15 分钟，验证码只能成功消费一次', CURRENT_TIMESTAMP, true, 0, 480000000000000100),
    (480000000000000013, 'PASSWORD_EMAIL_CODE_RESEND_SECONDS', '邮箱改密验证码重发间隔秒数', '60',
     '必须为正整数；限制同一账号和客户端短时间重复发送', CURRENT_TIMESTAMP, true, 0, 480000000000000100),
    (480000000000000014, 'PASSWORD_EMAIL_CODE_MAX_ATTEMPTS', '邮箱改密验证码最大尝试次数', '5',
     '必须为正整数；达到次数后验证码立即失效', CURRENT_TIMESTAMP, true, 0, 480000000000000100);
