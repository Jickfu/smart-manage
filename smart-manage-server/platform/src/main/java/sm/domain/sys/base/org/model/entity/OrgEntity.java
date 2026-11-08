package sm.domain.sys.base.org.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.domain.sys.base.org.model.OrgType;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 组织持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_org")
public class OrgEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 名称
     */
    private String name;
    /**
     * 父ID
     */
    private Long parentId;
    /**
     * 长编码
     */
    private String numberPath;
    /**
     * 长名称
     */
    private String namePath;
    /**
     * 组织类型：GROUP 集团、COMPANY 公司、DEPARTMENT 部门
     */
    private OrgType orgType;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 封存状态
     */
    private Boolean archived;
    /**
     * 封存时间
     */
    private LocalDateTime archivedAt;
    /**
     * 说明
     */
    private String description;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
