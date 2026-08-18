package sm.domain.sys.base.feature.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.form.FeatureListForm;
import sm.domain.sys.base.feature.model.form.FeatureSaveForm;
import sm.domain.sys.base.feature.model.vo.FeatureVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListSqlQuery;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeatureService {
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "featureKey", ListSqlQuery.string("a.feature_key", true),
            "name", ListSqlQuery.string("COALESCE(a.custom_name, a.default_name)", true),
            "appName", ListSqlQuery.string("b.name", false),
            "source", ListSqlQuery.enumeration("a.source", false),
            "seq", ListSqlQuery.number("COALESCE(a.custom_seq, a.default_seq)", true),
            "visible", ListSqlQuery.bool("a.visible", false));
    private final FeatureMapper mapper;
    private final FeatureTxService txService;

    public PageData<FeatureVO> listPage(FeatureListForm form) {
        Page<FeatureVO> result = mapper.selectListPage(new Page<>(form.getPageNum(), form.getPageSize()),
                form, ListSqlQuery.of(form, LIST_FIELDS));
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
    }

    public List<FeatureVO> listAllVisible() {
        return mapper.selectAllVisible();
    }

    public FeatureVO detail(Long id) {
        FeatureVO detail = mapper.selectDetailById(id);
        if (detail == null) throw new BizException(ResultEnum.NOT_FOUND, "功能不存在");
        return detail;
    }

    @BizLog("保存功能运营配置")
    public void save(FeatureSaveForm form) {
        txService.save(form);
    }
}
