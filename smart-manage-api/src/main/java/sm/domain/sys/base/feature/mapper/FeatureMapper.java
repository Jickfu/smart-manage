package sm.domain.sys.base.feature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.feature.model.form.FeatureListForm;
import sm.domain.sys.base.feature.model.vo.FeatureVO;

import java.util.List;

@Mapper
public interface FeatureMapper extends BaseMapper<FeatureEntity> {
    Page<FeatureVO> selectListPage(Page<FeatureVO> page, @Param("form") FeatureListForm form);
    FeatureVO selectDetailById(@Param("id") Long id);
    List<FeatureVO> selectAllVisible();
}
