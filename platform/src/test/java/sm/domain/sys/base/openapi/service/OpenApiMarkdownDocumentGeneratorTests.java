package sm.domain.sys.base.openapi.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.system.openapi.OpenApiOperationRegistry;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class OpenApiMarkdownDocumentGeneratorTests {
    @Test
    void generatesCommonMarkDocumentFromReleaseMetadata() {
        OpenApiReleaseEntity release = new OpenApiReleaseEntity();
        release.setName("查询 *基础资料*");
        release.setApiVersion("v1");
        release.setApiNumber("sys.basic-data.items");
        release.setOperationKey("sys.basicData.items.queryByCategory");
        release.setHttpMethod("POST");
        release.setPath("/openapi/items/query");
        release.setDomainName("系统管理");
        release.setApplicationName("基础平台");
        release.setFeatureName("基础资料");
        release.setStatus("PUBLISHED");
        release.setDescription("接口说明");
        release.setDocumentation("协议说明");
        release.setRequestExample("{\"categoryNumber\":\"demo\"}");
        release.setResponseExample("{\"items\":[{\"number\":\"A001\"}]}");
        release.setRequestSchema("{\"type\":\"object\",\"required\":[\"categoryNumber\"],"
                + "\"properties\":{\"categoryNumber\":{\"type\":\"string\"}}}");
        release.setResponseSchema("{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\","
                + "\"items\":{\"type\":\"object\",\"properties\":{\"number\":{\"type\":\"string\"},"
                + "\"parentNumber\":{\"type\":[\"string\",\"null\"]}}}}}}");

        String markdown = new String(new OpenApiMarkdownDocumentGenerator(
                JsonMapper.builder().build(), mock(OpenApiOperationRegistry.class))
                .generate(List.of(release)), StandardCharsets.UTF_8);

        assertThat(markdown).contains("# API 接口文档", "## 查询 \\*基础资料\\*",
                "|字段|内容|字段|内容|", "API 编码", "请求 URL", "用途说明",
                "### 请求体参数", "|参数名称|参数类型|必填|说明|层级|示例|", "categoryNumber", "|是|",
                "### 返回参数", "array\\<object\\>", "string \\| null", "A001",
                "### 请求结构示例", "### 返回结构示例", "```json");
        assertThat(markdown).doesNotContain("协议说明", "JSON Schema 无法解析");
    }

    @Test
    void rejectsInvalidSchemaInsteadOfExportingMisleadingPlaceholderRow() {
        OpenApiReleaseEntity release = new OpenApiReleaseEntity();
        release.setName("异常接口");
        release.setRequestSchema("{");

        OpenApiMarkdownDocumentGenerator generator = new OpenApiMarkdownDocumentGenerator(
                JsonMapper.builder().build(), mock(OpenApiOperationRegistry.class));

        assertThatThrownBy(() -> generator.generate(List.of(release)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OpenAPI JSON Schema 无法解析");
    }
}
