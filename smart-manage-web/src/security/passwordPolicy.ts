export const PASSWORD_POLICY_HINT =
  '15～64个字符，允许空格，无需混合大小写、数字和符号；不能使用弱口令或账号、系统名称的简单变体。';

/** 按 Unicode 码点计数，与后端一致；不截断或去除用户密码中的空格。 */
export const getPasswordPolicyError = (password: string): string | undefined => {
  if (!password || !password.trim()) return '密码不能为空或全为空白';
  const length = Array.from(password).length;
  if (length < 15 || length > 64) return '密码须为15～64个字符';
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
  return undefined;
};

export const passwordPolicyRule = {
  required: true,
  validator: (_rule: unknown, value?: string) => {
    const error = getPasswordPolicyError(value ?? '');
    return error ? Promise.reject(new Error(error)) : Promise.resolve();
  },
};
