package sm.domain.sys.base.basicdata.openapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.basicdata.openapi.BasicDataOpenApiContributor;
import sm.domain.sys.base.basicdata.openapi.service.BasicDataOpenApiService;
import sm.domain.sys.base.basicdata.openapi.model.form.BasicDataOpenApiQueryForm;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@Tag(name = "OpenAPI-基础资料", description = "经独立 OpenAPI 安全链路保护的基础资料接口")
public class BasicDataOpenApiController {
    private final BasicDataOpenApiService service;

    @PostMapping(BasicDataOpenApiContributor.PATH)
    public Result<BasicDataOpenApiService.BasicDataResponse> queryByCategory(
            @Valid @RequestBody BasicDataOpenApiQueryForm form) {
        return Result.success(service.queryByCategory(form.categoryNumber()));
    }
}
