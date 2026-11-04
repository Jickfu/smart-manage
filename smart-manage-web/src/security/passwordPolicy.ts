export interface PasswordPolicy {
  minLength: number;
  maxLength: number;
  complexityEnabled: boolean;
}

export const getPasswordPolicyHint = (policy: PasswordPolicy | undefined) =>
  policy
    ? `${policy.minLength}～${policy.maxLength}个字符${policy.complexityEnabled ? '，必须包含大写字母、小写字母、数字和特殊字符' : '，允许空格和长口令'}；不能使用弱口令或账号、系统名称的简单变体。`
    : '密码策略尚未加载';

/** 按 Unicode 码点计数，与后端一致；不截断或去除用户密码中的空格。 */
export const getPasswordPolicyError = (
  password: string,
  policy: PasswordPolicy,
): string | undefined => {
  if (!password || !password.trim()) return '密码不能为空或全为空白';
  const length = Array.from(password).length;
  if (length < policy.minLength || length > policy.maxLength)
    return `密码须为${policy.minLength}～${policy.maxLength}个字符`;
  if (
    Array.from(password).some((character) => {
      const codePoint = character.codePointAt(0)!;
      return (
        codePoint < 32 ||
        (codePoint >= 127 && codePoint <= 159) ||
        (codePoint >= 55296 && codePoint <= 57343)
      );
    })
  )
    return '密码不能包含控制字符或无效字符';
  if (
    policy.complexityEnabled &&
    (!/[A-Z]/.test(password) ||
      !/[a-z]/.test(password) ||
      !/[0-9]/.test(password) ||
      !/[\x21-\x2f\x3a-\x40\x5b-\x60\x7b-\x7e]/.test(password))
  )
    return '密码必须包含大写字母、小写字母、数字和特殊字符';
  return undefined;
};

export const createPasswordPolicyRule = (policy: PasswordPolicy | undefined) => ({
  required: true,
  validator: (_rule: unknown, value?: string) => {
    if (!policy) return Promise.reject(new Error('密码策略尚未加载，请稍后重试'));
    const error = getPasswordPolicyError(value ?? '', policy);
    return error ? Promise.reject(new Error(error)) : Promise.resolve();
  },
});
