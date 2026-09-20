package sm.domain.sys.base.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** 用户在指定组织下的共享授权快照。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorizationVO implements Serializable {
    private List<String> roleNumbers;
    private List<String> permissionNumbers;
}
