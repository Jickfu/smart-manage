package sm.domain.sys.base.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

/** 应用模块向同领域协作者提供的只读引用查询。 */
@Service
@RequiredArgsConstructor
public class AppReferenceService {
    private final AppMapper mapper;

    public AppEntity require(Long id) {
        AppEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "应用不存在");
        return entity;
    }

    public AppEntity findByNumber(String number) {
        return mapper.selectOne(new LambdaQueryWrapper<AppEntity>().eq(AppEntity::getNumber, number));
    }

    public List<AppEntity> findAll() {
        return mapper.selectList(null);
    }
}
