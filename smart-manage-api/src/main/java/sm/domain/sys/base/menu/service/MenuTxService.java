package sm.domain.sys.base.menu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;
import sm.domain.sys.base.menu.model.form.MenuSaveForm;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import sm.system.util.EnabledCommandUtil;

/**
 * 菜单事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class MenuTxService {
    private static final Pattern MENU_NUMBER_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");

    private final CurrentUserContext currentUserContext;
    private final MenuMapper mapper;
    private final PermissionMapper permissionMapper;
    private final FeatureMapper featureMapper;

    public Long save(MenuSaveForm form) {
        MenuEntity entity = new MenuEntity();
        if (form.getId() != null) {
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "编辑菜单时版本不能为空");
            }
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "菜单不存在");
            }
            if (!form.getVersion().equals(entity.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "菜单已被其他用户修改");
            }
        }
        if (form.getNumber() == null || !MENU_NUMBER_PATTERN.matcher(form.getNumber()).matches()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单编码必须为小写字母、数字和下划线，且以小写字母开头");
        }
        // 菜单编码会用于可分享的应用入口，创建后必须保持稳定。
        if (form.getId() != null && !java.util.Objects.equals(entity.getNumber(), form.getNumber())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单编码创建后不能修改");
        }
        Long duplicateCount = mapper.selectCount(new LambdaQueryWrapper<MenuEntity>()
                .eq(MenuEntity::getAppId, form.getAppId())
                .eq(MenuEntity::getNumber, form.getNumber())
                .ne(form.getId() != null, MenuEntity::getId, form.getId()));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "同一应用下菜单编码不能重复");
        }
        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        if (form.getLevel() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单层级不能为空");
        }
        // 菜单层级收敛为两级：分组/页面
        if (!(form.getLevel().equals(MenuLevelEnum.CATEGORY) || form.getLevel().equals(MenuLevelEnum.PAGE))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单层级只能是分组或页面");
        }
        // 页面可以位于应用根级，也可以归入同应用分组；页面始终必须关联功能和权限。
        if (form.getLevel().equals(MenuLevelEnum.PAGE)) {
            if (form.getParentId() != null && form.getParentId() > 0) {
                if (java.util.Objects.equals(form.getId(), form.getParentId())) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "页面菜单不能选择自身作为父分组");
                }
                MenuEntity parent = mapper.selectById(form.getParentId());
                if (parent == null || !MenuLevelEnum.CATEGORY.equals(parent.getLevel())
                        || !java.util.Objects.equals(parent.getAppId(), form.getAppId())) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "页面菜单父级必须是同一应用下的分组");
                }
            }
            if (form.getFeatureId() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "页面层级菜单必须选择所属功能");
            }
            if (form.getPermissionId() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "页面层级菜单必须选择权限");
            }
            if (form.getTargetType() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "页面层级菜单必须选择目标类型");
            }
            entity.setTargetType(form.getTargetType());
            if (MenuTargetTypeEnum.INTERNAL_PAGE.equals(form.getTargetType())) {
                if (form.getPath() == null || form.getPath().isBlank()) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "内部页面菜单必须填写路径");
                }
                if (form.getComponent() == null || form.getComponent().isBlank()) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "内部页面菜单必须填写组件");
                }
                entity.setPath(form.getPath().trim());
                entity.setComponent(form.getComponent().trim());
                entity.setExternalUrl(null);
                entity.setExternalOpenMode(null);
            } else if (MenuTargetTypeEnum.EXTERNAL_LINK.equals(form.getTargetType())) {
                String externalUrl = validateExternalUrl(form.getExternalUrl());
                if (form.getExternalOpenMode() == null) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "外部链接菜单必须选择打开方式");
                }
                entity.setPath(null);
                entity.setComponent(null);
                entity.setExternalUrl(externalUrl);
                entity.setExternalOpenMode(form.getExternalOpenMode());
            } else {
                throw new BizException(ResultEnum.PARAM_ERROR, "页面目标类型无效");
            }
        } else {
            if (form.getParentId() != null && form.getParentId() > 0) {
                throw new BizException(ResultEnum.PARAM_ERROR, "分组菜单必须位于应用根级");
            }
            if (form.getFeatureId() != null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "分组菜单不能选择所属功能");
            }
            if (form.getPermissionId() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "分组菜单必须选择应用级入口权限");
            }
            entity.setPath(null);
            entity.setComponent(null);
            entity.setTargetType(null);
            entity.setExternalUrl(null);
            entity.setExternalOpenMode(null);
        }
        entity.setPermissionId(form.getPermissionId());
        FeatureEntity feature = form.getFeatureId() == null ? null : featureMapper.selectById(form.getFeatureId());
        if (form.getFeatureId() != null
                && (feature == null || !java.util.Objects.equals(feature.getAppId(), form.getAppId()))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单与功能必须属于同一应用");
        }
        entity.setFeatureId(feature == null ? null : feature.getId());
        if (form.getPermissionId() != null) {
            PermissionEntity permission = permissionMapper.selectById(form.getPermissionId());
            boolean sameFeature = feature != null && permission != null
                    && java.util.Objects.equals(permission.getFeatureId(), feature.getId());
            boolean sameApplication = feature == null && permission != null && permission.getFeatureId() == null
                    && java.util.Objects.equals(permission.getAppId(), form.getAppId());
            if (!sameFeature && !sameApplication) {
                throw new BizException(ResultEnum.PARAM_ERROR, "菜单入口权限必须属于所选功能");
            }
        }
        entity.setLevel(form.getLevel());
        entity.setParentId(form.getParentId() != null ? form.getParentId() : 0L);
        entity.setAppId(form.getAppId());
        entity.setIcon(form.getIcon());
        entity.setDescription(form.getDescription());
        entity.setSort(form.getSort() != null ? form.getSort() : 99);
        if (form.getId() == null) {
            entity.setEnabled(true);
        }
        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            // 使用全字段 XML 更新，确保分组菜单可以把 permissionId/path/component 清空为 null。
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUser(currentUserContext.isLogin() ? currentUserContext.getUserId() : null);
            if (mapper.updateAllColumns(entity) != 1) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "菜单已被其他用户修改");
            }
        }
        return entity.getId();
    }

    /** 外链只允许浏览器可导航的绝对 HTTP(S) 地址，禁止可执行协议和内嵌凭据。 */
    private String validateExternalUrl(String rawExternalUrl) {
        if (rawExternalUrl == null || rawExternalUrl.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "外部链接菜单必须填写链接地址");
        }
        String externalUrl = rawExternalUrl.trim();
        try {
            URI uri = new URI(externalUrl);
            String scheme = uri.getScheme();
            boolean supportedScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!uri.isAbsolute() || !supportedScheme || uri.getRawAuthority() == null
                    || uri.getRawAuthority().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "外部链接必须是绝对 HTTP 或 HTTPS 地址");
            }
            if (uri.getUserInfo() != null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "外部链接不能包含账号密码");
            }
            return externalUrl;
        } catch (URISyntaxException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "外部链接格式无效");
        }
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单ID不能为空");
        }
        MenuEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "菜单不存在");
        }
        Long childCount = mapper.selectCount(new LambdaQueryWrapper<MenuEntity>()
                .eq(MenuEntity::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException(ResultEnum.FOREIGN_KEY_CONFLICT, "存在子菜单，不能删除");
        }
        if (mapper.deleteById(id) != 1) {
            throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户删除");
        }
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        EnabledCommandUtil.update(mapper, MenuEntity::getId, MenuEntity::getEnabled, ids, enabled, "菜单");
    }
}
