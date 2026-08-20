package sm.domain.sys.base.app.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.app.model.form.AppListForm;
import sm.domain.sys.base.app.model.form.AppSaveForm;
import sm.domain.sys.base.app.model.vo.*;
import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListSqlQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class AppService {
	private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
			"number", ListSqlQuery.string("a.number", true),
			"name", ListSqlQuery.string("a.name", true),
			"domainName", ListSqlQuery.string("b.name", false),
			"seq", ListSqlQuery.number("a.seq", true),
			"enabled", ListSqlQuery.bool("a.enabled", true),
			"description", ListSqlQuery.string("a.description", false),
			"createTime", ListSqlQuery.dateTime("a.create_time", true));
	private final CurrentUserContext currentUserContext;
	private final AppMapper mapper;
	private final AppTxService txService;

	public PageData<AppListVO> listPage(AppListForm form) {
		Page<AppListVO> result = mapper.selectListPage(new Page<>(form.getPageNum(), form.getPageSize()),
				form, ListSqlQuery.of(form, LIST_FIELDS));
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
	}

	/**
	 * 详情页需要展示所属领域信息（编码/名称），避免前端为 label 再请求一次 domain/detail。
	 */
	public AppDetailVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "应用ID不能为空");
		}
		AppDetailVO detail = mapper.selectDetailById(id);
		if (detail == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "应用不存在");
		}
		return detail;
	}

	public AppCreateNewDataVO createNewData() {
		AppCreateNewDataVO vo = new AppCreateNewDataVO();
		vo.setIcon(AppDefaults.ICON);
		vo.setIconColor(AppDefaults.ICON_COLOR);
		vo.setSeq(99);
		vo.setEnabled(true);
		return vo;
	}

	@BizLog("保存应用")
	public Long save(AppSaveForm form) {
		return txService.save(form);
	}

	@BizLog("删除应用")
	public void deleteById(Long id) {
		txService.deleteById(id);
	}

	@BizLog("启用应用")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
	}

	@BizLog("禁用应用")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
	}

	// ==================== 领域+应用入口查询 ====================

	public List<DomainAppsVO> getUserDomainApps(Long userId) {
		if (userId == null) {
			return List.of();
		}
		// 超级管理员拥有全部应用，不依赖用户角色关系。
		if (currentUserContext.isAdministrator()) {
			return getAllDomainApps();
		}
		return assembleDomainApps(mapper.selectUserDomainApps(userId, currentUserContext.getOrgId()));
	}

	public List<DomainAppsVO> getAllDomainApps() {
		return assembleDomainApps(mapper.selectAllDomainApps());
	}

	private List<DomainAppsVO> assembleDomainApps(List<DomainAppRowVO> rows) {
		Map<Long, DomainAppsVO> domainMap = new LinkedHashMap<>();
		Map<Long, Map<Long, AppVO>> appMap = new LinkedHashMap<>();
		for (DomainAppRowVO row : rows) {
			if (row.getDomainId() == null) {
				continue;
			}
			DomainAppsVO domain = domainMap.computeIfAbsent(row.getDomainId(), domainId -> {
				DomainAppsVO item = new DomainAppsVO();
				item.setId(domainId);
				item.setName(row.getDomainName());
				item.setNumber(row.getDomainNumber());
				item.setSeq(row.getDomainSeq());
				item.setAppList(new ArrayList<>());
				return item;
			});
			if (row.getAppId() == null) {
				continue;
			}
			Map<Long, AppVO> appsMap = appMap.computeIfAbsent(row.getDomainId(), domainId -> new LinkedHashMap<>());
			if (appsMap.containsKey(row.getAppId())) {
				continue;
			}
			AppVO vo = new AppVO();
			vo.setId(row.getAppId());
			vo.setName(row.getAppName());
			vo.setNumber(row.getAppNumber());
			vo.setIcon(row.getAppIcon());
			vo.setIconColor(row.getAppIconColor());
			vo.setSeq(row.getAppSeq());
			vo.setDescription(row.getAppDescription());
			appsMap.put(row.getAppId(), vo);
			domain.getAppList().add(vo);
		}
		return new ArrayList<>(domainMap.values());
	}

	public AppVO getUserAppByNumber(Long userId, String appNumber) {
		if (userId == null) {
			throw new BizException(ResultEnum.UNAUTHORIZED);
		}
		if (appNumber == null || appNumber.isBlank()) {
			throw new BizException(ResultEnum.PARAM_ERROR, "应用编码不能为空");
		}
		// 超级管理员直接按应用编码查询，普通用户仍通过角色权限关系过滤。
		AppVO vo = currentUserContext.isAdministrator()
				? mapper.selectAppByNumber(appNumber)
				: mapper.selectUserAppByNumber(userId, currentUserContext.getOrgId(), appNumber);
		if (vo == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "应用不存在或无权访问");
		}
		return vo;
	}
}
