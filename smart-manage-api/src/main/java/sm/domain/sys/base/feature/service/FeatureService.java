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

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureService {
    private final FeatureMapper mapper;
    private final FeatureTxService txService;

    public PageData<FeatureVO> listPage(FeatureListForm form) {
        Page<FeatureVO> result = mapper.selectListPage(new Page<>(form.getPageNum(), form.getPageSize()), form);
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
