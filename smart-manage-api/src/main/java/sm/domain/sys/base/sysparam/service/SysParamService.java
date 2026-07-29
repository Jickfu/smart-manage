package sm.domain.sys.base.sysparam.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.sysparam.model.entity.SysParamEntity;
import sm.domain.sys.base.sysparam.model.form.SysParamListForm;
import sm.domain.sys.base.sysparam.model.form.SysParamSaveForm;
import sm.domain.sys.base.sysparam.model.vo.SysParamCreateNewDataVO;
import sm.domain.sys.base.sysparam.model.vo.SysParamVO;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统参数服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysParamService {
    private final SysParamMapper mapper;
    private final SysParamTxService txService;

    /** 管理端分页列表 */
    public PageData<SysParamVO> listPage(SysParamListForm form) {
        Page<SysParamVO> result = mapper.selectListPage(
                new Page<>(form.getPageNum(), form.getPageSize()), form);
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
    }

    /** 详情 */
    public SysParamVO detail(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "系统参数ID不能为空");
        }
        SysParamVO detail = mapper.selectDetailById(id);
        if (detail == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "系统参数不存在");
        }
        return detail;
    }

    /** 新增默认值 */
    public SysParamCreateNewDataVO createNewData() {
        return new SysParamCreateNewDataVO();
    }

    /** 新增/编辑，委托事务服务处理 */
    @BizLog("保存系统参数")
    @CacheInvalidate(name = CacheConstant.SYS_PARAM,
            key = "T(sm.domain.sys.base.common.constant.CacheConstant).ALL_KEY")
    public Long save(SysParamSaveForm form) {
        return txService.save(form);
    }

    /** 删除，委托事务服务处理 */
    @BizLog("删除系统参数")
    @CacheInvalidate(name = CacheConstant.SYS_PARAM,
            key = "T(sm.domain.sys.base.common.constant.CacheConstant).ALL_KEY")
    public void deleteById(Long id) {
        txService.deleteById(id);
    }

    // ==================== 消费端（带缓存） ====================

    /** 全量获取 number → value 映射（Caffeine 本地缓存） */
    @Cached(cacheType = CacheType.LOCAL, name = CacheConstant.SYS_PARAM,
            key = "T(sm.domain.sys.base.common.constant.CacheConstant).ALL_KEY",
            expire = 30, timeUnit = TimeUnit.MINUTES)
    public Map<String, String> getAll() {
        List<SysParamEntity> entityList = mapper.selectList(null);
        Map<String, String> map = new HashMap<>();
        for (SysParamEntity entity : entityList) {
            map.put(entity.getNumber(), entity.getValue());
        }
        return map;
    }

    /** 获取字符串值 */
    public String getString(String number) {
        return getAll().get(number);
    }

    /** 获取布尔值（"true" 或 "1" 为 true，其余 false） */
    public boolean getBoolean(String number) {
        String value = getString(number);
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /** 获取整数值；参数不存在返回 null，参数格式错误直接暴露配置异常。 */
    public Integer getInt(String number) {
        String value = getString(number);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new BizException(ResultEnum.CONFIG_ERROR,
                    "系统参数 " + number + " 的值无法转换为整数: " + value);
        }
    }

    // ==================== 内部方法 ====================

}
