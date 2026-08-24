package sm.domain.scm.procurement.purchaserequisition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.domain.sys.base.datascope.model.DataScope;

import java.util.List;
import java.util.Map;

@Mapper
public interface PurchaseRequisitionMapper extends BaseMapper<PurchaseRequisitionEntity> {
    List<Map<String, Object>> selectStatusCounts(@Param("scope") DataScope scope);
}
