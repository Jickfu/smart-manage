package sm.domain.sys.base.sysparam.model.vo;

import lombok.Data;

/**
 * 系统参数 VO
 *
 * @author Chekfu
 */
@Data
public class SysParamVO {

    private Long id;

    private Integer version;

    private String number;

    private String name;

    private String value;

    private String remark;

    private Boolean isSystem;

    private Long appId;

    private String appName;
}
