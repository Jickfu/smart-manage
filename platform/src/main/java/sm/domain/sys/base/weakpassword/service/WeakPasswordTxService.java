package sm.domain.sys.base.weakpassword.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.model.entity.WeakPasswordEntity;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordDeleteForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordSaveForm;
import sm.domain.sys.base.weakpassword.util.WeakPasswordUtil;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class WeakPasswordTxService {
    private final WeakPasswordMapper mapper;

    public Long save(WeakPasswordSaveForm form) {
        String word = form.getWord();
        if (WeakPasswordUtil.isBlank(word) || word.codePointCount(0, word.length()) > 64
                || word.codePoints().anyMatch(codePoint -> Character.isISOControl(codePoint)
                || codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "弱口令须为1～64个字符，不能全为空白或包含控制字符");
        }
        WeakPasswordEntity entity = form.getId() == null ? new WeakPasswordEntity() : mapper.selectById(form.getId());
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "弱口令不存在");
        if (form.getId() != null && (form.getVersion() == null || !Objects.equals(entity.getVersion(), form.getVersion()))) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "弱口令已变化，请刷新后重试");
        }
        String digest = WeakPasswordUtil.digest(word);
        if (mapper.selectCount(new LambdaQueryWrapper<WeakPasswordEntity>()
                .eq(WeakPasswordEntity::getMatchDigest, digest)
                .ne(form.getId() != null, WeakPasswordEntity::getId, form.getId())) > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "弱口令已存在（匹配时忽略大小写）");
        }
        entity.setWord(word);
        entity.setMatchDigest(digest);
        entity.setDescription(form.getDescription());
        try {
            int affected = form.getId() == null ? mapper.insert(entity) : mapper.updateById(entity);
            if (affected != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "弱口令已变化，请刷新后重试");
        } catch (DuplicateKeyException exception) {
            // 唯一索引处理并发重复；不回显 SQL 中可能携带的词条。
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "弱口令已存在（匹配时忽略大小写）");
        }
        return entity.getId();
    }

    public void delete(WeakPasswordDeleteForm form) {
        if (mapper.delete(new LambdaQueryWrapper<WeakPasswordEntity>()
                .eq(WeakPasswordEntity::getId, form.getId())
                .eq(WeakPasswordEntity::getVersion, form.getVersion())) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "弱口令已变化或删除，请刷新后重试");
        }
    }
}
