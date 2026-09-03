-- 凭据安全状态由数据库统一推进，覆盖用户维护、导入、批量启停与全部改密入口。
ALTER TABLE t_sys_user ADD COLUMN credential_generation bigint NOT NULL DEFAULT 0
    CHECK (credential_generation >= 0);
COMMENT ON COLUMN t_sys_user.credential_generation IS '凭据安全代际，安全字段变化后旧验证码和会话失效';

CREATE FUNCTION advance_user_credential_generation() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.password IS DISTINCT FROM OLD.password
        OR NEW.password_reset IS DISTINCT FROM OLD.password_reset
        OR NEW.email IS DISTINCT FROM OLD.email
        OR NEW.email_verified_at IS DISTINCT FROM OLD.email_verified_at
        OR NEW.enabled IS DISTINCT FROM OLD.enabled THEN
        NEW.credential_generation := OLD.credential_generation + 1;
        -- MyBatis-Plus 可能已经递增 NEW.version；固定 OLD+1，不能重复递增。
        NEW.version := OLD.version + 1;
    ELSE
        NEW.credential_generation := OLD.credential_generation;
    END IF;
    RETURN NEW;
END;
$$;
COMMENT ON FUNCTION advance_user_credential_generation() IS '安全字段真实变化时原子推进凭据代际与编辑版本';
CREATE TRIGGER trg_user_credential_generation
BEFORE UPDATE ON t_sys_user
FOR EACH ROW EXECUTE FUNCTION advance_user_credential_generation();
