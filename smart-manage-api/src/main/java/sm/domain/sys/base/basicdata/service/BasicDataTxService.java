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
import sm.domain.sys.base.basicdata.model.form.BasicDataSaveForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.system.response.ResultEnum;
import sm.system.util.TransactionUtil;
import sm.system.util.EnabledCommandUtil;

import java.util.List;
import java.util.Objects;

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

        // 明细是聚合内部数据，保存时以请求中的 entrys 整体替换。
        entryMapper.delete(new LambdaQueryWrapper<BasicDataEntryEntity>()
                .eq(BasicDataEntryEntity::getParentId, entity.getId()));
        for (BasicDataEntryForm entryForm : form.getEntrys()) {
            BasicDataEntryEntity entry = new BasicDataEntryEntity();
            entry.setParentId(entity.getId());
            entry.setNumber(entryForm.getNumber());
            entry.setName(entryForm.getName());
            entry.setSort(entryForm.getSort() != null ? entryForm.getSort() : 99);
            entry.setEnabled(true);
            if (entryMapper.insert(entry) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
            }
        }

        String previousNumber = oldNumber;
        String currentNumber = entity.getNumber();
        TransactionUtil.afterCommit(() -> {
            removeOptionsCache(previousNumber);
            removeOptionsCache(currentNumber);
        });
        return entity.getId();
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "基础数据ID不能为空");
        }
        BasicDataEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "基础数据不存在");
        }

        // 主从聚合采用显式删除：先删除全部明细，再删除主表。
        entryMapper.delete(new LambdaQueryWrapper<BasicDataEntryEntity>()
                .eq(BasicDataEntryEntity::getParentId, id));
        if (mapper.deleteById(id) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "基础数据已被其他用户删除");
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
}
