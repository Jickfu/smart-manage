package sm.domain.sys.base.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "领域管理-列表项")
public class DomainListVO {
	private Long id;
	private String name;
	private String number;
	private Integer seq;
	private Boolean enabled;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}

