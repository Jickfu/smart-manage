package sm.domain.sys.base.role.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.role.contract.*;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.system.exception.BizException;
import sm.system.response.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleReferenceService implements RoleReferenceReader {
    private final RoleMapper mapper;
    @Override
    public PageData<RoleReference> search(String keyword, int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || keyword != null && keyword.length() > 100) throw new BizException(ResultEnum.PARAM_ERROR, "角色查询参数无效");
        var query = new LambdaQueryWrapper<RoleEntity>().orderByAsc(RoleEntity::getNumber).orderByAsc(RoleEntity::getId);
        if (keyword != null && !keyword.isBlank()) query.and(condition -> condition.like(RoleEntity::getName, keyword.trim()).or().like(RoleEntity::getNumber, keyword.trim()));
        var page = mapper.selectPage(Page.of(pageNum, pageSize), query);
        return PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords().stream().map(role -> new RoleReference(role.getId(), role.getNumber(), role.getName())).toList());
    }
    @Override
    public Map<Long, RoleReference> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        if (ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0)) throw new BizException(ResultEnum.PARAM_ERROR, "角色引用无效");
        var result = new LinkedHashMap<Long, RoleReference>();
        for (var role : mapper.selectByIds(ids)) result.put(role.getId(), new RoleReference(role.getId(), role.getNumber(), role.getName()));
        return Map.copyOf(result);
    }
}
