package sm.domain.sys.base.login.service;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.TACBuilder;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 行为验证码引擎配置，仅启用滑块拼图能力。 */
@Configuration
class CaptchaConfiguration {
    @Bean(destroyMethod = "close")
    ImageCaptchaApplication imageCaptchaApplication(RedisCaptchaCacheStore cacheStore) {
        // 资源仓库必须先设置再装载内置模板，这是验证码库 Builder 的调用顺序要求。
        return TACBuilder.builder()
                .setResourceStore(new LocalMemoryResourceStore())
                .addDefaultTemplate()
                .addResource("SLIDER", new Resource("classpath", "captcha/backgrounds/mountain-lake.jpg"))
                .addResource("SLIDER", new Resource("classpath", "captcha/backgrounds/autumn-forest.jpg"))
                .addResource("SLIDER", new Resource("classpath", "captcha/backgrounds/coastal-cliffs.jpg"))
                .addResource("SLIDER", new Resource("classpath", "captcha/backgrounds/tea-hills.jpg"))
                .setCacheStore(cacheStore)
                .prefix("sys:base:captcha-challenge")
                .build();
    }
}
