package sm.domain.sys.base.user.quicklaunch.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuickLaunchConfigurationVO {
    private List<QuickLaunchItemVO> options;
    private List<Long> selectedMenuIds;
}
