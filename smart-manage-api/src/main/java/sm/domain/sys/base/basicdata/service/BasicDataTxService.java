package sm.domain.sys.base.basicdata.service;

import com.alicp.jetcache.anno.CacheType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.basicdata.mapper.BasicDataEntryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntryEntity;
import sm.domain.sys.base.basicdata.model.form.BasicDataEntryForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataDeleteForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataSaveForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.system.response.ResultEnum;
import sm.system.util.TransactionUtil;
import sm.system.util.EnabledCommandUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础数据聚合内部事务实现，只允许 BasicDataService 委托调用。
 *
 * @author Chekfu
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class BasicDataTxService {
    private final BasicDataMapper mapper;
    private final BasicDataEntryMapper entryMapper;
    private final CacheHelper cacheHelper;

    public Long save(BasicDataSaveForm form) {
        normalizeAndValidate(form);
        BasicDataEntity entity;
        String oldNumber = null;
        if (form.getId() == null) {
            entity = new BasicDataEntity();
        } else {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "基础数据不存在");
            }
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "修改基础数据时乐观锁版本号不能为空");
            }
            if (!Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "基础数据已被其他用户修改，请刷新后重试");
            }
            oldNumber = entity.getNumber();
        }

        Long duplicateCount = mapper.selectCount(new LambdaQueryWrapper<BasicDataEntity>()
                .eq(BasicDataEntity::getNumber, form.getNumber())
                .ne(form.getId() != null, BasicDataEntity::getId, form.getId()));
        if (duplicateCount > 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "基础数据编码已存在");
        }

        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        entity.setRemark(form.getRemark());
        if (form.getId() == null) {
            entity.setEnabled(true);
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else if (mapper.updateById(entity) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "基础数据已被其他用户修改，请刷新后重试");
        }

        synchronizeEntries(entity.getId(), form.getEntrys());

        String previousNumber = oldNumber;
        String currentNumber = entity.getNumber();
        TransactionUtil.afterCommit(() -> {
            removeOptionsCache(previousNumber);
            removeOptionsCache(currentNumber);
        });
        return entity.getId();
    }

    public void delete(BasicDataDeleteForm form) {
        BasicDataEntity entity = mapper.selectById(form.getId());
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "基础数据不存在");
        }
        if (!Objects.equals(entity.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "基础数据已被其他用户修改，请刷新后重试");
        }

        // 主从聚合采用显式删除：先删除全部明细，再删除主表。
        entryMapper.delete(new LambdaQueryWrapper<BasicDataEntryEntity>()
                .eq(BasicDataEntryEntity::getParentId, form.getId()));
        int deleted = mapper.delete(new LambdaQueryWrapper<BasicDataEntity>()
                .eq(BasicDataEntity::getId, form.getId())
                .eq(BasicDataEntity::getVersion, form.getVersion()));
        if (deleted == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "基础数据已被其他用户修改或删除");
        }
        String deletedNumber = entity.getNumber();
        TransactionUtil.afterCommit(() -> removeOptionsCache(deletedNumber));
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        List<String> numbers = mapper.selectByIds(ids)
                .stream()
                .map(BasicDataEntity::getNumber)
                .toList();
        EnabledCommandUtil.update(mapper, BasicDataEntity::getId, BasicDataEntity::getEnabled,
                ids, enabled, "基础数据");
        TransactionUtil.afterCommit(() -> numbers.forEach(this::removeOptionsCache));
    }

    private void removeOptionsCache(String number) {
        if (number == null) {
            return;
        }
        cacheHelper.<String, List<BasicDataOptionVO>>getCache(
                CacheConstant.BASIC_DATA_OPTIONS, CacheType.LOCAL).remove(number);
    }

    private void normalizeAndValidate(BasicDataSaveForm form) {
        form.setNumber(form.getNumber().trim());
        form.setName(form.getName().trim());
        form.setRemark(form.getRemark() == null ? null : form.getRemark().trim());
        Set<String> numbers = form.getEntrys().stream()
                .peek(entry -> {
                    entry.setNumber(entry.getNumber().trim());
                    entry.setName(entry.getName().trim());
                    entry.setSort(entry.getSort() == null ? 0 : entry.getSort());
                    entry.setEnabled(entry.getEnabled() == null || entry.getEnabled());
                })
                .map(BasicDataEntryForm::getNumber)
                .collect(Collectors.toSet());
        if (numbers.size() != form.getEntrys().size()) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "同一基础数据下的明细编码不能重复");
        }
        long distinctIds = form.getEntrys().stream()
                .map(BasicDataEntryForm::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long idCount = form.getEntrys().stream().map(BasicDataEntryForm::getId).filter(Objects::nonNull).count();
        if (distinctIds != idCount) {
            throw new BizException(ResultEnum.PARAM_ERROR, "基础数据明细ID不能重复");
        }
    }

    /** 按明细 ID 增量同步，避免全删全插导致明细身份和审计信息丢失。 */
    private void synchronizeEntries(Long parentId, List<BasicDataEntryForm> forms) {
        Map<Long, BasicDataEntryEntity> existingById = entryMapper
                .selectList(new LambdaQueryWrapper<BasicDataEntryEntity>()
                        .eq(BasicDataEntryEntity::getParentId, parentId))
                .stream()
                .collect(Collectors.toMap(BasicDataEntryEntity::getId, Function.identity()));
        Set<Long> retainedIds = forms.stream()
                .map(BasicDataEntryForm::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> removedIds = existingById.keySet().stream()
                .filter(id -> !retainedIds.contains(id))
                .toList();
        if (!removedIds.isEmpty()) {
            entryMapper.deleteByIds(removedIds);
        }

        for (BasicDataEntryForm form : forms) {
            BasicDataEntryEntity entry;
            if (form.getId() == null) {
                entry = new BasicDataEntryEntity();
                entry.setParentId(parentId);
            } else {
                entry = existingById.get(form.getId());
                if (entry == null) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "基础数据明细不属于当前基础数据");
                }
            }
            entry.setNumber(form.getNumber());
            entry.setName(form.getName());
            entry.setSort(form.getSort());
            entry.setEnabled(form.getEnabled());
            int affected = form.getId() == null ? entryMapper.insert(entry) : entryMapper.updateById(entry);
            if (affected != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "基础数据明细写入失败");
            }
        }
    }
}
