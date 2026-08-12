package sm.domain.sys.base.feature.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_feature")
public class FeatureEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String featureKey;
    private Long appId;
    private String defaultName;
    private String customName;
    private Integer defaultSeq;
    private Integer customSeq;
    private String description;
    private Boolean visible;
    private String source;
    @Version
    private Integer version;
}
