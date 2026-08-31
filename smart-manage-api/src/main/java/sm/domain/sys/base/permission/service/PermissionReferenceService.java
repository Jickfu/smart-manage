package sm.domain.sys.base.permission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 权限模块向同领域协作者提供的只读引用查询。 */
@Service
@RequiredArgsConstructor
public class PermissionReferenceService {
    private final PermissionMapper mapper;

    public PermissionEntity require(Long id) {
        PermissionEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "权限不存在");
        return entity;
    }
}
