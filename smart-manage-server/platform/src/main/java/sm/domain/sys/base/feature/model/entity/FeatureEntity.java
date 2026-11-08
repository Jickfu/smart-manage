package sm.domain.sys.base.feature.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 功能持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_feature")
public class FeatureEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 稳定功能键
     */
    private String featureKey;
    /**
     * 所属应用ID
     */
    private Long appId;
    /**
     * 系统默认名称
     */
    private String defaultName;
    /**
     * 管理员自定义名称
     */
    private String customName;
    /**
     * 系统默认排序
     */
    private Integer defaultSeq;
    /**
     * 管理员自定义排序
     */
    private Integer customSeq;
    /**
     * 说明
     */
    private String description;
    /**
     * 是否在功能目录中可见
     */
    private Boolean visible;
    /**
     * 功能来源：SYSTEM 系统、PLUGIN 插件、EXTERNAL 外部
     */
    private String source;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
