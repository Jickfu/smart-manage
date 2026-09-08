package sm.domain.sys.base.weakpassword.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sm.domain.sys.base.weakpassword.model.entity.WeakPasswordEntity;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordListForm;
import sm.domain.sys.base.weakpassword.model.vo.WeakPasswordVO;
import sm.system.query.ListSqlQuery;

@Mapper
public interface WeakPasswordMapper extends BaseMapper<WeakPasswordEntity> {
    Page<WeakPasswordVO> selectListPage(Page<WeakPasswordVO> page,
            @Param("form") WeakPasswordListForm form, @Param("listQuery") ListSqlQuery listQuery);

    /** 安全策略直接读权威库，不允许会话或二级缓存复用旧黑名单结果。 */
    @Select("SELECT EXISTS (SELECT 1 FROM t_sys_weak_password a WHERE a.match_digest = #{digest})")
    @Options(useCache = false, flushCache = Options.FlushCachePolicy.TRUE)
    boolean containsDigest(@Param("digest") String digest);
}
