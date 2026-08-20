package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用管理-列表项")
public class AppListVO {
	private Long id;
	private String name;
	private String number;
	private String icon;
	private String iconColor;
	private Integer seq;
	private String description;
	private Long domainId;
	private String domainName;
	private Boolean enabled;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}

