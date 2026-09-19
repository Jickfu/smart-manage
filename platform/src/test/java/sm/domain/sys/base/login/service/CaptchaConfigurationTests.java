package sm.domain.sys.base.login.service;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CaptchaConfigurationTests {

    @Test
    void shouldRegisterMultipleReadableSliderBackgrounds() throws Exception {
        CaptchaConfiguration configuration = new CaptchaConfiguration();
        try (ImageCaptchaApplication application = configuration.imageCaptchaApplication(
                mock(RedisCaptchaCacheStore.class))) {
            List<Resource> resources = application.getImageCaptchaResourceManager()
                    .randomGetResource("SLIDER", null, 4);

            assertThat(resources).hasSize(4);
            for (Resource resource : resources) {
                try (InputStream input = application.getImageCaptchaResourceManager()
                        .getResourceInputStream(resource)) {
                    assertThat(input.read()).isNotEqualTo(-1);
                }
            }
        }
    }
}
