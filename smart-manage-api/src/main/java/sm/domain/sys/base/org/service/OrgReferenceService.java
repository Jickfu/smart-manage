package sm.domain.sys.base.org.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 组织模块对外提供的只读引用服务。 */
@Service
@RequiredArgsConstructor
public class OrgReferenceService implements OrgReferenceReader {

    private final OrgMapper mapper;

    @Override
    public OrgReference require(Long orgId) {
        if (orgId == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "组织ID不能为空");
        }
        OrgEntity entity = mapper.selectById(orgId);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "组织不存在");
        }
        return new OrgReference(entity.getId(), entity.getParentId(), entity.getNumber(), entity.getName(), entity.getNumberPath(), entity.getNamePath(), entity.getOrgType().name(),
                Boolean.TRUE.equals(entity.getEnabled()), Boolean.TRUE.equals(entity.getArchived()));
    }

    @Override
    public OrgReference requireAvailable(Long orgId) {
        OrgReference reference = require(orgId);
        if (!reference.enabled() || reference.archived()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "组织已停用或已封存");
        }
        return reference;
    }

    @Override
    public Map<Long, OrgReference> findByIds(Collection<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) return Map.of();
        Map<Long, OrgReference> result = new LinkedHashMap<>();
        for (OrgEntity entity : mapper.selectByIds(orgIds)) {
            OrgReference reference = toReference(entity);
            result.put(reference.id(), reference);
        }
        return result;
    }

    @Override
    public List<OrgReference> findAll() {
        return mapper.selectList(null).stream().map(this::toReference).toList();
    }

    private OrgReference toReference(OrgEntity entity) {
        return new OrgReference(entity.getId(), entity.getParentId(), entity.getNumber(), entity.getName(),
                entity.getNumberPath(), entity.getNamePath(), entity.getOrgType().name(), Boolean.TRUE.equals(entity.getEnabled()), Boolean.TRUE.equals(entity.getArchived()));
    }
}
