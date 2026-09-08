package sm.domain.sys.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.vo.SchedulerCatalogNodeVO;
import sm.domain.sys.scheduler.model.vo.SchedulerCatalogRowVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 调度列表专用目录，目录查询沿用各自列表权限。 */
@Service
@RequiredArgsConstructor
public class SchedulerCatalogService {
    private final JobMapper jobMapper;
    private final JobLogMapper jobLogMapper;

    public List<SchedulerCatalogNodeVO> jobs() {
        return assembleCatalog(jobMapper.selectCatalog());
    }

    public List<SchedulerCatalogNodeVO> executions() {
        return assembleCatalog(jobLogMapper.selectCatalog());
    }

    private List<SchedulerCatalogNodeVO> assembleCatalog(List<SchedulerCatalogRowVO> rows) {
        Map<Long, SchedulerCatalogNodeVO> domains = new LinkedHashMap<>();
        for (SchedulerCatalogRowVO row : rows) {
            SchedulerCatalogNodeVO domain = domains.computeIfAbsent(row.getDomainId(), domainId ->
                    new SchedulerCatalogNodeVO("domain:" + domainId, row.getDomainName(), new ArrayList<>()));
            if (row.getAppId() != null) {
                domain.children().add(new SchedulerCatalogNodeVO(
                        "app:" + row.getDomainId() + ":" + row.getAppId(), row.getAppName(), List.of()));
            }
        }
        return new ArrayList<>(domains.values());
    }
}
