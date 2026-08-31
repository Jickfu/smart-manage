package sm.domain.sys.base.basicdata.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.basicdata.mapper.BasicDataCategoryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataItemMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataItemEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 基础资料的稳定外部投影，不暴露内部雪花 ID。 */
@Service
@RequiredArgsConstructor
public class BasicDataOpenApiService {
    private final BasicDataCategoryMapper categoryMapper;
    private final BasicDataItemMapper itemMapper;

    public BasicDataResponse queryByCategory(String categoryNumber) {
        BasicDataCategoryEntity category = categoryMapper.selectOne(
                new LambdaQueryWrapper<BasicDataCategoryEntity>()
                        .eq(BasicDataCategoryEntity::getNumber, categoryNumber));
        if (category == null || !Boolean.TRUE.equals(category.getEnabled())) {
            throw new BizException(ResultEnum.NOT_FOUND, "基础资料分类不存在或未启用");
        }
        List<BasicDataItemEntity> entities = itemMapper.selectList(
                new LambdaQueryWrapper<BasicDataItemEntity>()
                        .eq(BasicDataItemEntity::getCategoryId, category.getId())
                        .orderByAsc(BasicDataItemEntity::getNumberPath));
        Map<Long, BasicDataItemEntity> entityById = new HashMap<>();
        for (BasicDataItemEntity entity : entities) {
            entityById.put(entity.getId(), entity);
        }
        List<BasicDataItem> items = new ArrayList<>();
        for (BasicDataItemEntity entity : entities) {
            if (!Boolean.TRUE.equals(entity.getIsLeaf()) || !isEffectivelyEnabled(entity, entityById)) {
                continue;
            }
            BasicDataItemEntity parent = entity.getParentId() == null
                    ? null : entityById.get(entity.getParentId());
            items.add(new BasicDataItem(entity.getNumber(), entity.getName(),
                    parent == null ? null : parent.getNumber(), entity.getNumberPath(), entity.getNamePath()));
        }
        return new BasicDataResponse(category.getNumber(), category.getName(), List.copyOf(items));
    }

    private boolean isEffectivelyEnabled(BasicDataItemEntity entity,
                                         Map<Long, BasicDataItemEntity> entityById) {
        BasicDataItemEntity current = entity;
        while (current != null) {
            if (!Boolean.TRUE.equals(current.getEnabled())) {
                return false;
            }
            current = current.getParentId() == null ? null : entityById.get(current.getParentId());
        }
        return true;
    }

    public record BasicDataResponse(String categoryNumber, String categoryName,
                                    List<BasicDataItem> items) {
    }

    public record BasicDataItem(String number, String name, String parentNumber,
                                String numberPath, String namePath) {
    }
}


