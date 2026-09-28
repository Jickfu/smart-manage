package sm.domain.workflow.process.task.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.workflow.process.task.mapper.TaskMonitorMapper;
import sm.domain.workflow.process.task.model.form.TaskMonitorListForm;
import sm.domain.workflow.process.task.model.vo.TaskMonitorRow;
import sm.domain.workflow.process.task.model.vo.TaskMonitorVO;
import sm.system.response.PageData;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskMonitorService {
    private final TaskMonitorMapper mapper;
    private final UserReferenceReader users;

    public PageData<TaskMonitorVO> listPage(TaskMonitorListForm form) {
        String keyword = form.getKeyword() == null || form.getKeyword().isBlank() ? null : form.getKeyword().trim();
        var page = mapper.listPage(Page.of(form.getPageNum(), form.getPageSize()), keyword);
        var candidateIds = page.getRecords().stream().flatMap(row -> candidates(row).stream())
                .collect(java.util.stream.Collectors.toSet());
        var names = users.findByIds(candidateIds).values().stream()
                .collect(java.util.stream.Collectors.toMap(UserReference::id, UserReference::name));
        var records = page.getRecords().stream().map(row -> view(row, names)).toList();
        return PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    private TaskMonitorVO view(TaskMonitorRow row, java.util.Map<Long, String> names) {
        List<Long> candidates = candidates(row);
        var candidateNames = candidates.stream().filter(names::containsKey)
                .collect(java.util.stream.Collectors.toMap(id -> id, names::get));
        return new TaskMonitorVO(row.getId(), row.getInstanceId(), row.getNumber(), row.getBusinessType(),
                row.getNodeCode(), row.getNodeName(), Boolean.TRUE.equals(row.getActive()), candidates,
                candidateNames, row.getCreateTime());
    }

    private List<Long> candidates(TaskMonitorRow row) {
        return row.getCandidateIds() == null || row.getCandidateIds().isBlank()
                ? List.of() : java.util.Arrays.stream(row.getCandidateIds().split(","))
                .filter(value -> !value.isBlank()).map(Long::valueOf).toList();
    }
}
