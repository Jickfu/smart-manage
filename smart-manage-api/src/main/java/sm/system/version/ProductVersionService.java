package sm.system.version;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

/** 读取资源过滤生成的产品版本，IDEA 与 Maven 使用相同的资源文件。 */
@Service
public class ProductVersionService {
    private final ProductVersion version;

    public ProductVersionService(ObjectProvider<BuildProperties> buildPropertiesProvider) {
        BuildProperties properties = buildPropertiesProvider.getIfAvailable();
        String productVersion = properties == null ? null : properties.getVersion();
        // 未经资源处理的源码占位符不能作为有效版本展示。
        if (productVersion != null && (productVersion.isBlank() || productVersion.contains("@"))) {
            productVersion = null;
        }
        version = new ProductVersion(productVersion);
    }

    public ProductVersion current() {
        return version;
    }
}
