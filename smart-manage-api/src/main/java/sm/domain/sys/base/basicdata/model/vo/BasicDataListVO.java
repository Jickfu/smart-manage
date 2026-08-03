package sm.domain.sys.base.basicdata.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础数据列表项
 *
 * @author Chekfu
 */
@Data
@Schema(description = "基础数据列表项")
public class BasicDataListVO {

    private Long id;

    private String number;

    private String name;

    private String remark;

    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer version;
}
