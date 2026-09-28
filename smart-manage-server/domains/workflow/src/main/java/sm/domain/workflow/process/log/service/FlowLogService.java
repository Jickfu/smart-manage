package sm.domain.workflow.process.log.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.workflow.process.log.mapper.FlowLogMapper;
import sm.domain.workflow.process.log.model.form.FlowLogListForm;
import sm.domain.workflow.process.log.model.vo.FlowLogRow;
import sm.system.response.PageData;
import sm.domain.sys.base.user.contract.UserReferenceReader;

@Service
@RequiredArgsConstructor
public class FlowLogService {
    private final FlowLogMapper mapper;
    private final UserReferenceReader users;

    public PageData<FlowLogRow> listPage(FlowLogListForm form) {
        String keyword = form.getKeyword() == null || form.getKeyword().isBlank() ? null : form.getKeyword().trim();
        var page = mapper.listPage(Page.of(form.getPageNum(), form.getPageSize()), keyword);
        var names = users.findByIds(page.getRecords().stream().map(FlowLogRow::getOperatorId)
                .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet()));
        page.getRecords().forEach(row -> {
            var user = names.get(row.getOperatorId());
            if (user != null) row.setOperatorName(user.name());
        });
        return PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
