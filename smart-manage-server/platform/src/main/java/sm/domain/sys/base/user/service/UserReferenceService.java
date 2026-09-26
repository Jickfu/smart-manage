package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** 用户跨领域引用服务，只发布稳定身份和启用状态。 */
@Service
@RequiredArgsConstructor
public class UserReferenceService implements UserReferenceReader {

    private final UserMapper mapper;

    @Override
    public sm.system.response.PageData<UserReference> searchEnabled(String keyword, int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || keyword != null && keyword.length() > 100) {
            throw new BizException(ResultEnum.PARAM_ERROR, "人员查询参数无效");
        }
        var query = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEnabled, true).orderByAsc(UserEntity::getNumber).orderByAsc(UserEntity::getId);
        if (keyword != null && !keyword.isBlank()) query.and(condition -> condition.like(UserEntity::getName, keyword.trim())
                .or().like(UserEntity::getNumber, keyword.trim()).or().like(UserEntity::getUsername, keyword.trim()));
        var page = mapper.selectPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize), query);
        return sm.system.response.PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords().stream().map(this::toReference).toList());
    }

    @Override
    public UserReference require(Long userId) {
        requireUserId(userId);
        UserEntity user = mapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        }
        return toReference(user);
    }

    @Override
    public UserReference requireEnabled(Long userId) {
        UserReference reference = require(userId);
        requireEnabled(reference);
        return reference;
    }

    @Override
    public Map<Long, UserReference> findByIds(Collection<Long> userIds) {
        List<Long> normalizedUserIds = normalizeUserIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return Map.of();
        }
        return referencesInInputOrder(normalizedUserIds, mapper.selectByIds(normalizedUserIds));
    }

    @Override
    public Map<Long, UserReference> requireEnabledByIds(Collection<Long> userIds) {
        List<Long> normalizedUserIds = normalizeUserIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserReference> references = referencesInInputOrder(
                normalizedUserIds, mapper.selectByIds(normalizedUserIds));
        for (Long userId : normalizedUserIds) {
            UserReference reference = references.get(userId);
            if (reference == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "用户不存在: " + userId);
            }
            requireEnabled(reference);
        }
        return references;
    }

    private List<Long> normalizeUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> distinctUserIds = new LinkedHashSet<>();
        for (Long userId : userIds) {
            requireUserId(userId);
            distinctUserIds.add(userId);
        }
        return List.copyOf(distinctUserIds);
    }

    private Map<Long, UserReference> referencesInInputOrder(
            List<Long> userIds, List<UserEntity> users) {
        Map<Long, UserReference> referenceById = new LinkedHashMap<>();
        for (UserEntity user : users) {
            UserReference reference = toReference(user);
            referenceById.put(reference.id(), reference);
        }
        Map<Long, UserReference> orderedReferences = new LinkedHashMap<>();
        for (Long userId : userIds) {
            UserReference reference = referenceById.get(userId);
            if (reference != null) {
                orderedReferences.put(userId, reference);
            }
        }
        return Collections.unmodifiableMap(orderedReferences);
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "用户ID不能为空");
        }
    }

    private void requireEnabled(UserReference reference) {
        if (!reference.enabled()) {
            throw new BizException(ResultEnum.PARAM_ERROR,
                    "用户已禁用，不能作为业务引用: " + reference.id());
        }
    }

    private UserReference toReference(UserEntity user) {
        return new UserReference(
                user.getId(), user.getNumber(), user.getName(), user.getUsername(), user.getEmail(),
                Boolean.TRUE.equals(user.getEnabled()));
    }
}
