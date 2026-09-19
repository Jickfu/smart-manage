package sm.domain.sys.base.login.service;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.login.model.form.CaptchaTrackForm;
import sm.domain.sys.base.login.model.form.CaptchaTrackPointForm;
import sm.domain.sys.base.login.model.vo.CaptchaChallengeVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.List;

/** 隔离第三方验证码协议，只向登录服务暴露项目自己的模型。 */
@Component
@RequiredArgsConstructor
class SliderCaptchaGateway {
    private final ImageCaptchaApplication captchaApplication;

    public CaptchaChallengeVO createChallenge() {
        ApiResponse<ImageCaptchaVO> response = captchaApplication.generateCaptcha("SLIDER");
        if (!response.isSuccess() || response.getData() == null) {
            throw new BizException(ResultEnum.SERVER_ERROR, "验证码生成失败");
        }
        ImageCaptchaVO source = response.getData();
        CaptchaChallengeVO target = new CaptchaChallengeVO();
        target.setChallengeId(source.getId());
        target.setBackgroundImage(source.getBackgroundImage());
        target.setTemplateImage(source.getTemplateImage());
        target.setBackgroundImageWidth(source.getBackgroundImageWidth());
        target.setBackgroundImageHeight(source.getBackgroundImageHeight());
        target.setTemplateImageWidth(source.getTemplateImageWidth());
        target.setTemplateImageHeight(source.getTemplateImageHeight());
        return target;
    }

    public boolean verify(String challengeId, CaptchaTrackForm form) {
        ApiResponse<?> response = captchaApplication.matching(challengeId, toTrack(form));
        return response.isSuccess();
    }

    private ImageCaptchaTrack toTrack(CaptchaTrackForm form) {
        ImageCaptchaTrack track = new ImageCaptchaTrack();
        track.setBgImageWidth(form.getBackgroundImageWidth());
        track.setBgImageHeight(form.getBackgroundImageHeight());
        track.setTemplateImageWidth(form.getTemplateImageWidth());
        track.setTemplateImageHeight(form.getTemplateImageHeight());
        track.setStartTime(form.getStartTime());
        track.setStopTime(form.getStopTime());
        track.setLeft(form.getLeft());
        track.setTop(form.getTop());
        track.setTrackList(toTrackPoints(form.getTrackList()));
        return track;
    }

    private List<ImageCaptchaTrack.Track> toTrackPoints(List<CaptchaTrackPointForm> source) {
        List<ImageCaptchaTrack.Track> result = new ArrayList<>(source.size());
        for (CaptchaTrackPointForm point : source) {
            result.add(new ImageCaptchaTrack.Track(point.getX(), point.getY(), point.getT(), point.getType()));
        }
        return result;
    }
}
