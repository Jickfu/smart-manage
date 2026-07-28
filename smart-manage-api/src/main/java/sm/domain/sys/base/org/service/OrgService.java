package sm.domain.sys.base.org.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.form.OrgListForm;
import sm.domain.sys.base.org.model.vo.OrgListVO;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.system.response.PageData;

import java.util.List;

/**
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrgService {
	private final OrgMapper mapper;
	private final OrgConverter converter;

	public PageData<OrgListVO> listPage(OrgListForm form) {
		LambdaQueryWrapper<OrgEntity> qw = new LambdaQueryWrapper<OrgEntity>()
				.orderByAsc(OrgEntity::getSort)
				.orderByAsc(OrgEntity::getId);
		Page<OrgEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<OrgEntity> result = mapper.selectPage(page, qw);
		List<OrgListVO> vos = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

}
