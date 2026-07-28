package sm.domain.sys.base.basicdata.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.basicdata.mapper.BasicDataEntryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntryEntity;
import sm.domain.sys.base.basicdata.model.form.BasicDataListForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataSaveForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataCreateNewDataVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataDetailVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataEntryVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.List;

/**
 * 基础数据聚合的唯一公开服务。
 *
 * @author Chekfu
 */
@Service
@RequiredArgsConstructor
public class BasicDataService {
    private final BasicDataMapper mapper;
    private final BasicDataEntryMapper entryMapper;
    private final BasicDataTxService txService;
    private final BasicDataConverter converter;

    public PageData<BasicDataListVO> listPage(BasicDataListForm form) {
        LambdaQueryWrapper<BasicDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            queryWrapper.and(condition -> condition.like(BasicDataEntity::getName, keyword)
                    .or().like(BasicDataEntity::getNumber, keyword));
        }
        queryWrapper.orderByAsc(BasicDataEntity::getNumber);
        Page<BasicDataEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        Page<BasicDataEntity> result = mapper.selectPage(page, queryWrapper);
        List<BasicDataListVO> records = result.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    /** 查询主表及全部明细，一次返回完整聚合。 */
    public BasicDataDetailVO detail(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "基础数据ID不能为空");
        }
        BasicDataEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "基础数据不存在");
        }
        BasicDataDetailVO detailVO = new BasicDataDetailVO();
        detailVO.setId(entity.getId());
        detailVO.setNumber(entity.getNumber());
        detailVO.setName(entity.getName());
        detailVO.setRemark(entity.getRemark());
        detailVO.setEnabled(entity.getEnabled());
        detailVO.setCreateTime(entity.getCreateTime());
        detailVO.setUpdateTime(entity.getUpdateTime());
        detailVO.setVersion(entity.getVersion());
        detailVO.setEntrys(listEntries(entity.getId()));
        return detailVO;
    }

    public BasicDataCreateNewDataVO createNewData() {
        BasicDataCreateNewDataVO createNewDataVO = new BasicDataCreateNewDataVO();
        createNewDataVO.setEnabled(true);
        return createNewDataVO;
    }

    /** 按基础数据编码提供启用的下拉选项，不开放明细独立写入口。 */
    @Cached(cacheType = CacheType.LOCAL, name = CacheConstant.BASIC_DATA_OPTIONS,
            key = "#number", expire = 30, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public List<BasicDataOptionVO> getOptionsByNumber(String number) {
        BasicDataEntity entity = mapper.selectOne(new LambdaQueryWrapper<BasicDataEntity>()
                .eq(BasicDataEntity::getNumber, number));
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "基础数据不存在");
        }
        return entryMapper.selectList(new LambdaQueryWrapper<BasicDataEntryEntity>()
                        .eq(BasicDataEntryEntity::getParentId, entity.getId())
                        .eq(BasicDataEntryEntity::getEnabled, true)
                        .orderByAsc(BasicDataEntryEntity::getSort)
                        .orderByAsc(BasicDataEntryEntity::getId))
                .stream()
                .map(entry -> new BasicDataOptionVO(entry.getNumber(), entry.getName()))
                .toList();
    }

    @BizLog("保存基础数据")
    public Long save(BasicDataSaveForm form) {
        return txService.save(form);
    }

    @BizLog("删除基础数据")
    public void deleteById(Long id) {
        txService.deleteById(id);
    }

    @BizLog("启用基础数据")
    public void enable(List<Long> ids) {
        txService.updateEnabled(ids, true);
    }

    @BizLog("禁用基础数据")
    public void disable(List<Long> ids) {
        txService.updateEnabled(ids, false);
    }

    private List<BasicDataEntryVO> listEntries(Long parentId) {
        return entryMapper.selectList(new LambdaQueryWrapper<BasicDataEntryEntity>()
                        .eq(BasicDataEntryEntity::getParentId, parentId)
                        .orderByAsc(BasicDataEntryEntity::getSort)
                        .orderByAsc(BasicDataEntryEntity::getId))
                .stream()
                .map(converter::toEntryVO)
                .toList();
    }

}
