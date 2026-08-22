package sm.domain.sys.message.email.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.message.email.controller.EmailController.*;
import sm.domain.sys.message.email.mapper.*;
import sm.domain.sys.message.email.model.entity.*;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.helper.SM4Helper;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.util.TraceIdUtil;
import sm.system.query.ListQueryUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final Set<String> SECURITY_MODES = Set.of("NONE", "STARTTLS", "SSL_TLS");
    private static final Set<String> TASK_STATUSES = Set.of("PENDING","SENDING","SUCCESS","RETRY_WAIT","FAILED","UNKNOWN","CANCELLED");
    private static final Pattern DANGEROUS_HTML = Pattern.compile("(?is)<\\s*(script|iframe|object|embed|form|base)\\b|on[a-z]+\\s*=|javascript\\s*:");
    private static final Map<String, ListQueryUtil.Field<EmailAccountEntity>> ACCOUNT_LIST_FIELDS = Map.of(
            "number", ListQueryUtil.string(EmailAccountEntity::getNumber, true),
            "name", ListQueryUtil.string(EmailAccountEntity::getName, true),
            "fromAddress", ListQueryUtil.string(EmailAccountEntity::getFromAddress, false),
            "securityMode", ListQueryUtil.enumeration(EmailAccountEntity::getSecurityMode, false),
            "enabled", ListQueryUtil.bool(EmailAccountEntity::getEnabled, true),
            "defaultAccount", ListQueryUtil.bool(EmailAccountEntity::getDefaultAccount, false));
    private static final Map<String, ListQueryUtil.Field<EmailTaskEntity>> RECORD_LIST_FIELDS = Map.of(
            "subject", ListQueryUtil.string(EmailTaskEntity::getSubject, true),
            "toAddresses", ListQueryUtil.string(EmailTaskEntity::getToAddresses, false),
            "accountNumber", ListQueryUtil.string(EmailTaskEntity::getAccountNumber, false),
            "status", ListQueryUtil.enumeration(EmailTaskEntity::getStatus, true),
            "attemptCount", ListQueryUtil.number(EmailTaskEntity::getAttemptCount, false),
            "createTime", ListQueryUtil.dateTime(EmailTaskEntity::getCreateTime, true));
    private final CurrentUserContext currentUserContext;
    private final EmailAccountMapper accountMapper;
    private final UserMapper userMapper;
    private final EmailTaskMapper taskMapper;
    private final EmailAttemptMapper attemptMapper;
    private final EmailTxService txService;
    private final SM4Helper sm4Helper;
    private final JsonMapper jsonMapper;
    @Value("${smart-manage.instance-id:unknown}") private String instanceId;

    public PageData<Map<String, Object>> accountList(AccountListForm form) {
        currentUserContext.checkAdministrator();
        LambdaQueryWrapper<EmailAccountEntity> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(form.getKeyword())) query.and(w -> w.like(EmailAccountEntity::getNumber, form.getKeyword().trim()).or().like(EmailAccountEntity::getName, form.getKeyword().trim()));
        if (form.getEnabled() != null) query.eq(EmailAccountEntity::getEnabled, form.getEnabled());
        ListQueryUtil.apply(query, form, ACCOUNT_LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) query.orderByDesc(EmailAccountEntity::getDefaultAccount).orderByAsc(EmailAccountEntity::getNumber);
        Page<EmailAccountEntity> page = accountMapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), page.getRecords().stream().map(this::accountMap).toList());
    }

    public Map<String, Object> accountDetail(Long id) { currentUserContext.checkAdministrator(); return accountMap(txService.requireAccount(id)); }

    @BizLog(value="保存发信账号", recordRequest=false)
    public Long saveAccount(AccountSaveForm form) {
        currentUserContext.checkAdministrator();
        if (!SECURITY_MODES.contains(form.securityMode())) throw new BizException(ResultEnum.PARAM_ERROR, "不支持的 SMTP 安全模式");
        try { return txService.saveAccount(form); }
        catch (DuplicateKeyException exception) { throw new BizException(ResultEnum.DATA_CONFLICT, "账号编码已存在或默认账号发生冲突"); }
    }

    @BizLog("启停发信账号") public void enableAccount(AccountEnableForm form) { currentUserContext.checkAdministrator(); txService.enable(form.id(), form.version(), form.enabled()); }
    @BizLog("删除发信账号") public void deleteAccount(Long id, Integer version) { currentUserContext.checkAdministrator(); txService.delete(id, version); }

    public List<Map<String, Object>> manualAccountOptions() {
        currentUserContext.checkAdministrator();
        return accountMapper.selectList(new LambdaQueryWrapper<EmailAccountEntity>().eq(EmailAccountEntity::getEnabled, true).eq(EmailAccountEntity::getAllowManual, true).orderByDesc(EmailAccountEntity::getDefaultAccount).orderByAsc(EmailAccountEntity::getName))
                .stream().map(account -> map("id", account.getId(), "number", account.getNumber(), "name", account.getName(), "defaultAccount", account.getDefaultAccount(), "fromAddress", account.getFromAddress())).toList();
    }

    @BizLog(value="测试发信账号", recordRequest=false)
    public String testAccount(AccountTestForm form) {
        currentUserContext.checkAdministrator();
        EmailAccountEntity account = txService.requireAccount(form.accountId());
        if (StringUtils.hasText(form.recipient())) {
            sendSmtp(account, List.of(form.recipient().trim()), List.of(), List.of(), "Smart Manage 邮件配置测试", "<p>这是一封来自 Smart Manage 的 SMTP 配置测试邮件。</p>", "这是一封来自 Smart Manage 的 SMTP 配置测试邮件。");
            return "测试邮件已被 SMTP 服务器接受";
        }
        testConnection(account);
        return "SMTP 连接与认证成功";
    }

    @BizLog(value="管理员发送邮件", recordRequest=false)
    public Long compose(ComposeForm form) {
        currentUserContext.checkAdministrator();
        validateRecipientCount(form.toUserIds(), form.ccUserIds(), form.bccUserIds());
        if (DANGEROUS_HTML.matcher(form.htmlBody()).find()) throw new BizException(ResultEnum.PARAM_ERROR, "邮件正文包含脚本、事件处理器或危险链接");
        EmailAccountEntity account = resolveManualAccount(form.accountId());
        EmailTaskEntity task = new EmailTaskEntity();
        task.setSceneKey("admin.manual"); task.setIdempotencyKey("admin.manual:" + UUID.randomUUID());
        task.setAccountId(account.getId()); task.setAccountNumber(account.getNumber()); task.setFromAddress(account.getFromAddress()); task.setFromName(account.getFromName());
        task.setToAddresses(json(resolveUserAddresses(form.toUserIds())));
        task.setCcAddresses(json(resolveUserAddresses(form.ccUserIds())));
        task.setBccAddresses(json(resolveUserAddresses(form.bccUserIds())));
        task.setSubject(form.subject().trim()); task.setHtmlBody(form.htmlBody()); task.setTextBody(form.textBody());
        task.setStatus("PENDING"); task.setAttemptCount(0); task.setMaxAttempts(3); task.setNextAttemptTime(LocalDateTime.now()); task.setTraceId(TraceIdUtil.getTraceId());
        return txService.insertTask(task);
    }

    public PageData<Map<String, Object>> recordList(RecordListForm form) {
        currentUserContext.checkAdministrator();
        LambdaQueryWrapper<EmailTaskEntity> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(form.getKeyword())) query.and(w -> w.like(EmailTaskEntity::getSubject, form.getKeyword().trim()).or().like(EmailTaskEntity::getToAddresses, form.getKeyword().trim()));
        if (StringUtils.hasText(form.getStatus())) {
            if (!TASK_STATUSES.contains(form.getStatus())) throw new BizException(ResultEnum.PARAM_ERROR, "邮件状态不合法");
            query.eq(EmailTaskEntity::getStatus, form.getStatus());
        }
        if (form.getAccountId() != null) query.eq(EmailTaskEntity::getAccountId, form.getAccountId());
        ListQueryUtil.apply(query, form, RECORD_LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) query.orderByDesc(EmailTaskEntity::getCreateTime);
        if (!ListQueryUtil.isSortedBy(form, "id")) query.orderByDesc(EmailTaskEntity::getId);
        Page<EmailTaskEntity> page = taskMapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), page.getRecords().stream().map(this::taskListMap).toList());
    }

    public Map<String, Object> recordDetail(Long id) {
        currentUserContext.checkAdministrator();
        EmailTaskEntity task = txService.requireTask(id);
        List<Map<String,Object>> attempts = attemptMapper.selectList(new LambdaQueryWrapper<EmailAttemptEntity>().eq(EmailAttemptEntity::getTaskId, id).orderByAsc(EmailAttemptEntity::getAttemptNo)).stream().map(this::attemptMap).toList();
        Map<String,Object> result = new LinkedHashMap<>(taskListMap(task));
        result.put("cc", addresses(task.getCcAddresses())); result.put("bcc", addresses(task.getBccAddresses())); result.put("htmlBody", task.getHtmlBody()); result.put("textBody", task.getTextBody()); result.put("attempts", attempts);
        return result;
    }

    @BizLog("重新发送邮件")
    public Long retry(Long id) {
        currentUserContext.checkAdministrator();
        EmailTaskEntity source = txService.requireTask(id);
        if (!"FAILED".equals(source.getStatus()) && !"UNKNOWN".equals(source.getStatus()) && !"CANCELLED".equals(source.getStatus())) throw new BizException(ResultEnum.DATA_CONFLICT, "只有失败、未知或已取消的邮件可以重新发送");
        EmailAccountEntity account = txService.requireAccount(source.getAccountId());
        if (!Boolean.TRUE.equals(account.getEnabled())) throw new BizException(ResultEnum.CONFIG_ERROR, "原发信账号已停用，不能自动切换账号");
        EmailTaskEntity task = copyTask(source); task.setSourceTaskId(source.getId()); task.setIdempotencyKey("retry:" + source.getId() + ":" + UUID.randomUUID());
        return txService.insertTask(task);
    }

    @BizLog("取消邮件发送") public void cancel(Long id, Integer version) { currentUserContext.checkAdministrator(); txService.cancel(id, version); }

    /** Quartz 集群任务入口；任务领取 SQL 使用 SKIP LOCKED，确保多实例不会正常重复领取。 */
    public int dispatchPending(int batchSize) {
        int safeBatchSize = Math.max(1, Math.min(batchSize, 100));
        List<EmailTaskEntity> tasks = taskMapper.claim(LocalDateTime.now(), safeBatchSize);
        for (EmailTaskEntity task : tasks) dispatch(task);
        return tasks.size();
    }

    private void dispatch(EmailTaskEntity task) {
        int attemptNo = task.getAttemptCount() + 1;
        EmailAttemptEntity attempt = new EmailAttemptEntity();
        attempt.setTaskId(task.getId()); attempt.setAttemptNo(attemptNo); attempt.setStatus("SENDING"); attempt.setStartedTime(LocalDateTime.now()); attempt.setInstanceId(instanceId); attempt.setTraceId(TraceIdUtil.getTraceId());
        task.setAttemptCount(attemptNo);
        try {
            EmailAccountEntity account = txService.requireAccount(task.getAccountId());
            if (!Boolean.TRUE.equals(account.getEnabled())) throw new MailConfigurationException("ACCOUNT_DISABLED", "发信账号已停用");
            sendSmtp(account, addresses(task.getToAddresses()), addresses(task.getCcAddresses()), addresses(task.getBccAddresses()), task.getSubject(), task.getHtmlBody(), task.getTextBody());
            LocalDateTime completed = LocalDateTime.now(); attempt.setStatus("SUCCESS"); attempt.setCompletedTime(completed); task.setStatus("SUCCESS"); task.setCompletedTime(completed); task.setErrorCategory(null); task.setErrorMessage(null);
        } catch (Exception exception) {
            String category = exception instanceof MailConfigurationException configuration ? configuration.category : classify(exception);
            String message = safeMessage(exception);
            boolean retryable = !(exception instanceof MailConfigurationException) && attemptNo < task.getMaxAttempts();
            attempt.setStatus("FAILED"); attempt.setCompletedTime(LocalDateTime.now()); attempt.setErrorCategory(category); attempt.setErrorMessage(message);
            task.setStatus(retryable ? "RETRY_WAIT" : "FAILED"); task.setNextAttemptTime(retryable ? LocalDateTime.now().plusMinutes(1L << (attemptNo - 1)) : null); task.setCompletedTime(retryable ? null : LocalDateTime.now()); task.setErrorCategory(category); task.setErrorMessage(message);
            log.warn("邮件投递失败: taskId={}, accountNumber={}, category={}, attempt={}", task.getId(), task.getAccountNumber(), category, attemptNo);
        }
        txService.finishAttempt(task, attempt);
    }

    private void testConnection(EmailAccountEntity account) {
        try (Transport transport = session(account).getTransport("smtp")) { transport.connect(account.getHost(), account.getPort(), account.getUsername(), sm4Helper.decrypt(account.getPasswordCipher())); }
        catch (MessagingException exception) { throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "SMTP 连接或认证失败: " + safeMessage(exception)); }
    }

    private void sendSmtp(EmailAccountEntity account, List<String> to, List<String> cc, List<String> bcc, String subject, String htmlBody, String textBody) {
        try {
            MimeMessage message = new MimeMessage(session(account));
            message.setFrom(new InternetAddress(account.getFromAddress(), account.getFromName(), java.nio.charset.StandardCharsets.UTF_8.name()));
            addRecipients(message, Message.RecipientType.TO, to); addRecipients(message, Message.RecipientType.CC, cc); addRecipients(message, Message.RecipientType.BCC, bcc);
            if (StringUtils.hasText(account.getReplyTo())) message.setReplyTo(new Address[]{new InternetAddress(account.getReplyTo())});
            message.setSubject(subject, java.nio.charset.StandardCharsets.UTF_8.name());
            MimeMultipart alternative = new MimeMultipart("alternative");
            if (StringUtils.hasText(textBody)) { MimeBodyPart plain = new MimeBodyPart(); plain.setText(textBody, java.nio.charset.StandardCharsets.UTF_8.name()); alternative.addBodyPart(plain); }
            MimeBodyPart html = new MimeBodyPart(); html.setContent(htmlBody, "text/html; charset=UTF-8"); alternative.addBodyPart(html); message.setContent(alternative);
            Transport.send(message, account.getUsername(), sm4Helper.decrypt(account.getPasswordCipher()));
        } catch (Exception exception) { if (exception instanceof RuntimeException runtime) throw runtime; throw new IllegalStateException(exception); }
    }

    private Session session(EmailAccountEntity account) {
        Properties properties = new Properties(); properties.put("mail.smtp.auth", "true"); properties.put("mail.smtp.host", account.getHost()); properties.put("mail.smtp.port", account.getPort().toString());
        properties.put("mail.smtp.connectiontimeout", account.getConnectionTimeoutMs().toString()); properties.put("mail.smtp.timeout", account.getReadTimeoutMs().toString()); properties.put("mail.smtp.writetimeout", account.getReadTimeoutMs().toString());
        if ("STARTTLS".equals(account.getSecurityMode())) { properties.put("mail.smtp.starttls.enable", "true"); properties.put("mail.smtp.starttls.required", "true"); }
        if ("SSL_TLS".equals(account.getSecurityMode())) properties.put("mail.smtp.ssl.enable", "true");
        return Session.getInstance(properties);
    }

    private EmailAccountEntity resolveManualAccount(Long id) {
        EmailAccountEntity account = id == null ? accountMapper.selectOne(new LambdaQueryWrapper<EmailAccountEntity>().eq(EmailAccountEntity::getDefaultAccount, true)) : txService.requireAccount(id);
        if (account == null) throw new BizException(ResultEnum.CONFIG_ERROR, "尚未配置全局默认发信账号");
        if (!Boolean.TRUE.equals(account.getEnabled())) throw new BizException(ResultEnum.CONFIG_ERROR, "发信账号已停用");
        if (id != null && !Boolean.TRUE.equals(account.getAllowManual())) throw new BizException(ResultEnum.PERMISSION_ERROR, "该账号不允许管理员手工选择");
        return account;
    }

    private Map<String,Object> accountMap(EmailAccountEntity value) { return map("id",value.getId(),"number",value.getNumber(),"name",value.getName(),"host",value.getHost(),"port",value.getPort(),"securityMode",value.getSecurityMode(),"username",value.getUsername(),"passwordConfigured",StringUtils.hasText(value.getPasswordCipher()),"fromAddress",value.getFromAddress(),"fromName",value.getFromName(),"replyTo",value.getReplyTo(),"enabled",value.getEnabled(),"defaultAccount",value.getDefaultAccount(),"allowManual",value.getAllowManual(),"connectionTimeoutMs",value.getConnectionTimeoutMs(),"readTimeoutMs",value.getReadTimeoutMs(),"description",value.getDescription(),"version",value.getVersion(),"createTime",value.getCreateTime(),"updateTime",value.getUpdateTime()); }
    private Map<String,Object> taskListMap(EmailTaskEntity value) { return map("id",value.getId(),"sourceTaskId",value.getSourceTaskId(),"sceneKey",value.getSceneKey(),"accountId",value.getAccountId(),"accountNumber",value.getAccountNumber(),"fromAddress",value.getFromAddress(),"fromName",value.getFromName(),"to",addresses(value.getToAddresses()),"subject",value.getSubject(),"status",value.getStatus(),"attemptCount",value.getAttemptCount(),"maxAttempts",value.getMaxAttempts(),"nextAttemptTime",value.getNextAttemptTime(),"completedTime",value.getCompletedTime(),"errorCategory",value.getErrorCategory(),"errorMessage",value.getErrorMessage(),"traceId",value.getTraceId(),"createUser",value.getCreateUser(),"createTime",value.getCreateTime(),"version",value.getVersion()); }
    private Map<String,Object> attemptMap(EmailAttemptEntity value) { return map("id",value.getId(),"attemptNo",value.getAttemptNo(),"status",value.getStatus(),"startedTime",value.getStartedTime(),"completedTime",value.getCompletedTime(),"errorCategory",value.getErrorCategory(),"errorMessage",value.getErrorMessage(),"instanceId",value.getInstanceId(),"traceId",value.getTraceId()); }
    private EmailTaskEntity copyTask(EmailTaskEntity source) { EmailTaskEntity task=new EmailTaskEntity(); task.setSceneKey(source.getSceneKey()); task.setAccountId(source.getAccountId()); task.setAccountNumber(source.getAccountNumber()); task.setFromAddress(source.getFromAddress()); task.setFromName(source.getFromName()); task.setToAddresses(source.getToAddresses()); task.setCcAddresses(source.getCcAddresses()); task.setBccAddresses(source.getBccAddresses()); task.setSubject(source.getSubject()); task.setHtmlBody(source.getHtmlBody()); task.setTextBody(source.getTextBody()); task.setStatus("PENDING"); task.setAttemptCount(0); task.setMaxAttempts(source.getMaxAttempts()); task.setNextAttemptTime(LocalDateTime.now()); task.setTraceId(TraceIdUtil.getTraceId()); return task; }
    private static void validateRecipientCount(List<?> to,List<?> cc,List<?> bcc) { int count=size(to)+size(cc)+size(bcc); if(count>50) throw new BizException(ResultEnum.PARAM_ERROR,"单封邮件收件地址合计不能超过 50 个"); }
    private static int size(List<?> values){return values==null?0:values.size();}
    /** 收件人引用只提交用户 ID，邮箱在管理员命令的服务端边界内解析，避免列表接口泄露隐私字段。 */
    private List<String> resolveUserAddresses(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        List<Long> distinctIds = userIds.stream().distinct().toList();
        Map<Long, UserEntity> users = userMapper.selectBatchIds(distinctIds).stream()
                .collect(java.util.stream.Collectors.toMap(UserEntity::getId, user -> user));
        List<String> addresses = new ArrayList<>(distinctIds.size());
        for (Long userId : distinctIds) {
            UserEntity user = users.get(userId);
            if (user == null) throw new BizException(ResultEnum.NOT_FOUND, "收件用户不存在: " + userId);
            if (!Boolean.TRUE.equals(user.getEnabled())) throw new BizException(ResultEnum.PARAM_ERROR, "收件用户已停用: " + user.getName());
            if (!StringUtils.hasText(user.getEmail())) throw new BizException(ResultEnum.PARAM_ERROR, "收件用户未配置邮箱: " + user.getName());
            addresses.add(user.getEmail().trim());
        }
        return addresses;
    }
    private static void addRecipients(MimeMessage message, Message.RecipientType type,List<String> addresses)throws MessagingException{for(String address:addresses)message.addRecipient(type,new InternetAddress(address,true));}
    private String json(List<String> values){try{return jsonMapper.writeValueAsString(values);}catch(JacksonException exception){throw new BizException(ResultEnum.PARAM_ERROR,"邮件地址序列化失败");}}
    private List<String> addresses(String json){if(!StringUtils.hasText(json))return List.of();try{return jsonMapper.readValue(json,new TypeReference<>(){});}catch(JacksonException exception){throw new IllegalStateException("邮件地址快照损坏",exception);}}
    private static String classify(Exception exception){Throwable cause=exception;while(cause.getCause()!=null)cause=cause.getCause();return cause instanceof java.net.SocketTimeoutException?"TIMEOUT":cause instanceof AuthenticationFailedException?"AUTHENTICATION":"SMTP_ERROR";}
    private static String safeMessage(Throwable exception){String message=exception.getMessage();if(!StringUtils.hasText(message))return exception.getClass().getSimpleName();message=message.replaceAll("(?i)(password|authorization|token)\\s*[=:]\\s*[^,;\\s]+","$1=[REDACTED]");return message.length()>1000?message.substring(0,1000):message;}
    private static Map<String,Object> map(Object... entries){Map<String,Object> result=new LinkedHashMap<>();for(int index=0;index<entries.length;index+=2)result.put((String)entries[index],entries[index+1]);return result;}
    private static final class MailConfigurationException extends RuntimeException { private final String category; private MailConfigurationException(String category,String message){super(message);this.category=category;} }
}
