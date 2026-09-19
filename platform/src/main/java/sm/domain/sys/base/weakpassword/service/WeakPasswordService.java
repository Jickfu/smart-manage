package sm.domain.sys.base.weakpassword.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.weakpassword.converter.WeakPasswordConverter;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordDeleteForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordListForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordSaveForm;
import sm.domain.sys.base.weakpassword.model.vo.WeakPasswordVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.query.ListSqlQuery;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnly;
import java.util.Map;

/** 词库维护仅允许真实管理员；认证读取使用独立的密码策略入口。 */
@Service
@RequiredArgsConstructor
@AdministratorOnly
public class WeakPasswordService {
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "word", ListSqlQuery.string("a.word", true),
            "description", ListSqlQuery.string("a.description", false));
    private final WeakPasswordMapper mapper;
    private final WeakPasswordConverter converter;
    private final WeakPasswordTxService txService;

    public PageData<WeakPasswordVO> listPage(WeakPasswordListForm form) {
        Page<WeakPasswordVO> result = mapper.selectListPage(new Page<>(form.getPageNum(), form.getPageSize()),
                form, ListSqlQuery.of(form, LIST_FIELDS));
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
    }

    public WeakPasswordVO detail(Long id) {
        var entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "弱口令不存在");
        return converter.toVO(entity);
    }

    @BizLog(value = "保存弱口令", recordRequest = false, recordResponse = false)
    public Long save(WeakPasswordSaveForm form) { return txService.save(form); }

    @BizLog("删除弱口令")
    public void delete(WeakPasswordDeleteForm form) { txService.delete(form); }
}
