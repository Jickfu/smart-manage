package sm.domain.sys.message.email.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.message.email.mapper.*;
import sm.domain.sys.message.email.model.entity.*;
import sm.domain.sys.message.email.model.form.AccountSaveForm;
import sm.system.exception.BizException;
import sm.system.helper.SM4Helper;
import sm.system.response.ResultEnum;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class EmailTxService {
    private final EmailAccountMapper accountMapper;
    private final EmailTaskMapper taskMapper;
    private final EmailAttemptMapper attemptMapper;
    private final SM4Helper sm4Helper;

    Long saveAccount(AccountSaveForm form) {
        EmailAccountEntity entity = form.id() == null ? new EmailAccountEntity() : requireAccount(form.id());
        if (form.id() != null && !Objects.equals(entity.getVersion(), form.version())) conflict();
        if (form.id() == null) entity.setEnabled(false);
        if (form.id() == null && (form.password() == null || form.password().isBlank())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "SMTP 密码或授权码不能为空");
        }
        if (Boolean.TRUE.equals(form.defaultAccount()) && !Boolean.TRUE.equals(entity.getEnabled())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "停用的发信账号不能设为默认账号，请先在列表中启用");
        }
        if (Boolean.TRUE.equals(form.defaultAccount())) {
            for (EmailAccountEntity other : accountMapper.selectList(new LambdaQueryWrapper<EmailAccountEntity>()
                    .eq(EmailAccountEntity::getDefaultAccount, true)
                    .ne(form.id() != null, EmailAccountEntity::getId, form.id()))) {
                other.setDefaultAccount(false);
                accountMapper.updateById(other);
            }
        }
        entity.setNumber(form.number().trim()); entity.setName(form.name().trim()); entity.setHost(form.host().trim());
        entity.setPort(form.port()); entity.setSecurityMode(form.securityMode()); entity.setUsername(form.username().trim());
        if (form.password() != null && !form.password().isBlank()) entity.setPasswordCipher(sm4Helper.encrypt(form.password()));
        entity.setFromAddress(form.fromAddress().trim()); entity.setFromName(trim(form.fromName())); entity.setReplyTo(trim(form.replyTo()));
        entity.setDefaultAccount(form.defaultAccount()); entity.setAllowManual(form.allowManual());
        entity.setConnectionTimeoutMs(form.connectionTimeoutMs()); entity.setReadTimeoutMs(form.readTimeoutMs()); entity.setDescription(trim(form.description()));
        int changed = form.id() == null ? accountMapper.insert(entity) : accountMapper.updateById(entity);
        if (changed != 1) conflict();
        return entity.getId();
    }

    void enable(Long id, Integer version, boolean enabled) {
        EmailAccountEntity entity = requireAccount(id);
        if (!Objects.equals(entity.getVersion(), version)) conflict();
        entity.setEnabled(enabled);
        if (!enabled) entity.setDefaultAccount(false);
        if (accountMapper.updateById(entity) != 1) conflict();
    }

    void delete(Long id, Integer version) {
        EmailAccountEntity entity = requireAccount(id);
        if (!Objects.equals(entity.getVersion(), version)) conflict();
        if (taskMapper.selectCount(new LambdaQueryWrapper<EmailTaskEntity>().eq(EmailTaskEntity::getAccountId, id)) > 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "发信账号已有发送记录，只能停用");
        }
        if (accountMapper.deleteById(entity) != 1) conflict();
    }

    Long insertTask(EmailTaskEntity task) {
        if (taskMapper.insert(task) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "创建邮件投递任务失败");
        return task.getId();
    }

    void cancel(Long id, Integer version) {
        EmailTaskEntity task = requireTask(id);
        if (!Objects.equals(task.getVersion(), version)) conflict();
        if (!"PENDING".equals(task.getStatus()) && !"RETRY_WAIT".equals(task.getStatus())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "只有等待发送的邮件可以取消");
        }
        task.setStatus("CANCELLED"); task.setCompletedTime(LocalDateTime.now());
        if (taskMapper.updateById(task) != 1) conflict();
    }

    void finishAttempt(EmailTaskEntity task, EmailAttemptEntity attempt) {
        attemptMapper.insert(attempt);
        if (taskMapper.updateById(task) != 1) throw new IllegalStateException("邮件任务状态更新失败");
    }

    EmailAccountEntity requireAccount(Long id) {
        EmailAccountEntity entity = accountMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "发信账号不存在");
        return entity;
    }
    EmailTaskEntity requireTask(Long id) {
        EmailTaskEntity entity = taskMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "邮件发送记录不存在");
        return entity;
    }
    private static String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static void conflict() { throw new BizException(ResultEnum.DATA_CONFLICT, "数据已被其他用户修改，请刷新后重试"); }
}
