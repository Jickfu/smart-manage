package sm.domain.sys.base.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.domain.mapper.DomainMapper;
import sm.domain.sys.base.domain.model.entity.DomainEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

/** 领域目录模块向同领域协作者提供的只读引用查询。 */
@Service
@RequiredArgsConstructor
public class DomainReferenceService {
    private final DomainMapper mapper;

    public DomainEntity require(Long id) {
        DomainEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "领域不存在");
        return entity;
    }

    public List<DomainEntity> findAll() {
        return mapper.selectList(null);
    }
}
