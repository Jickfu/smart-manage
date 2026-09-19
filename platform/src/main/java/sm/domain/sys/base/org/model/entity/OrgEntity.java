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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_org")
public class OrgEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String number;
    private String name;
    private Long parentId;
    private String numberPath;
    private String namePath;
    private OrgType orgType;
    private Integer sort;
    private Boolean enabled;
    private Boolean archived;
    private LocalDateTime archivedAt;
    private String description;
    @Version
    private Integer version;
}
