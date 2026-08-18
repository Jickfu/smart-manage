package sm.domain.sys.base.numberrule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleEntity;
import sm.domain.sys.base.numberrule.model.form.NumberRuleListForm;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleVO;
import sm.domain.sys.base.numberrule.model.vo.NumberReferenceVO;

import java.util.List;
import sm.system.query.ListSqlQuery;

@Mapper
public interface NumberRuleMapper extends BaseMapper<NumberRuleEntity> {
    Page<NumberRuleVO> selectListPage(Page<NumberRuleVO> page, @Param("form") NumberRuleListForm form,
                                      @Param("listQuery") ListSqlQuery listQuery);

    NumberRuleVO selectDetailById(@Param("id") Long id);

    List<NumberReferenceVO> selectReferences();

    Long nextValue(@Param("ruleKey") String ruleKey, @Param("scopeKey") String scopeKey,
                   @Param("periodKey") String periodKey, @Param("startValue") Long startValue);

    Long currentValue(@Param("ruleKey") String ruleKey, @Param("scopeKey") String scopeKey,
                      @Param("periodKey") String periodKey);

    long countAutomaticCategoryReferences(@Param("ruleKey") String ruleKey);

    long countDefaultReferences(@Param("ruleKey") String ruleKey);

    long countCounters(@Param("ruleKey") String ruleKey);
}
