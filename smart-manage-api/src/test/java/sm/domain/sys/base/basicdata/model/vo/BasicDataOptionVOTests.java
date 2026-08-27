package sm.domain.sys.base.basicdata.model.vo;

import com.alicp.jetcache.support.JavaValueEncoder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BasicDataOptionVOTests {

    @Test
    void supportsActualRemoteCacheEncoding() {
        BasicDataOptionVO option = new BasicDataOptionVO(
                1L, null, "ITEM-001", "基础数据", "基础数据", true);

        byte[] encoded = JavaValueEncoder.INSTANCE.apply(List.of(option));

        assertNotNull(encoded);
    }
}
