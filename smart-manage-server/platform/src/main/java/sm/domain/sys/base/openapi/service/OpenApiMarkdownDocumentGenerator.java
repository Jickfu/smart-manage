package sm.domain.sys.base.openapi.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Document;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Text;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.system.openapi.OpenApiOperationRegistry;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 从权威 API 版本元数据构造与详情页同构的 CommonMark 文档。 */
@Component
public class OpenApiMarkdownDocumentGenerator {
    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());
    private final MarkdownRenderer renderer = MarkdownRenderer.builder().extensions(EXTENSIONS).build();
    private final JsonMapper jsonMapper;
    private final OpenApiOperationRegistry operationRegistry;

    public OpenApiMarkdownDocumentGenerator(JsonMapper jsonMapper, OpenApiOperationRegistry operationRegistry) {
        this.jsonMapper = jsonMapper;
        this.operationRegistry = operationRegistry;
    }

    public byte[] generate(List<OpenApiReleaseEntity> releases) {
        Document document = new Document();
        appendHeading(document, 1, "API 接口文档");
        for (OpenApiReleaseEntity release : releases) {
            appendHeading(document, 2, release.getName());
            appendHeading(document, 3, "接口基本信息");
            appendBasicInfoTable(document, release);
            appendHeading(document, 3, "请求体参数");
            appendSchemaTable(document, schemaRows(release.getRequestSchema(), release.getRequestExample()), true);
            appendHeading(document, 3, "返回参数");
            appendSchemaTable(document, schemaRows(release.getResponseSchema(), release.getResponseExample()), false);
            appendHeading(document, 3, "请求结构示例");
            appendJson(document, valueOrEmptyObject(release.getRequestExample()));
            appendHeading(document, 3, "返回结构示例");
            appendJson(document, valueOrEmptyObject(release.getResponseExample()));
        }
        return renderer.render(document).getBytes(StandardCharsets.UTF_8);
    }

    private List<SchemaRow> schemaRows(String schemaJson, String exampleJson) {
        try {
            JsonNode schema = jsonMapper.readTree(schemaJson);
            JsonNode example = exampleJson == null ? null : jsonMapper.readTree(exampleJson);
            List<SchemaRow> rows = new ArrayList<>();
            appendProperties(rows, schema == null ? null : schema.get("properties"), requiredNames(schema),
                    1, example);
            return rows;
        } catch (Exception exception) {
            // 版本 Schema 是导出文档的权威结构，异常时必须阻断，不能生成误导性的占位参数。
            throw new IllegalArgumentException("OpenAPI JSON Schema 无法解析", exception);
        }
    }

    private void appendProperties(List<SchemaRow> rows, JsonNode properties, Set<String> required,
                                  int level, JsonNode example) {
        if (properties == null || !properties.isObject()) return;
        Iterator<Map.Entry<String, JsonNode>> fields = properties.properties().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String name = field.getKey();
            JsonNode property = field.getValue();
            JsonNode propertyExample = example == null ? null : example.get(name);
            rows.add(new SchemaRow(name, typeName(property), required.contains(name),
                    textOrDash(property.get("description")), level,
                    exampleText(propertyExample, property)));
            JsonNode nestedSchema = hasType(property, "array") ? property.get("items") : property;
            JsonNode nestedExample = propertyExample != null && propertyExample.isArray() && !propertyExample.isEmpty()
                    ? propertyExample.get(0) : propertyExample;
            if (nestedSchema != null) {
                appendProperties(rows, nestedSchema.get("properties"), requiredNames(nestedSchema),
                        level + 1, nestedExample);
            }
        }
    }

    private Set<String> requiredNames(JsonNode schema) {
        Set<String> names = new HashSet<>();
        if (schema == null || schema.get("required") == null) return names;
        schema.get("required").forEach(node -> names.add(node.asText()));
        return names;
    }

    private String typeName(JsonNode schema) {
        JsonNode type = schema.get("type");
        String name;
        if (type != null && type.isArray()) {
            List<String> names = new ArrayList<>();
            type.forEach(node -> names.add(node.asText()));
            name = String.join(" | ", names);
        } else {
            name = text(type);
        }
        if (name == null || name.isBlank()) name = "object";
        if ("array".equals(name) && schema.get("items") != null) {
            return "array<" + typeName(schema.get("items")) + ">";
        }
        return name;
    }

    private boolean hasType(JsonNode schema, String expectedType) {
        JsonNode type = schema == null ? null : schema.get("type");
        if (type == null || type.isNull()) return false;
        if (type.isArray()) {
            for (JsonNode candidate : type) {
                if (expectedType.equals(text(candidate))) return true;
            }
            return false;
        }
        return expectedType.equals(text(type));
    }

    private String exampleText(JsonNode example, JsonNode schema) {
        JsonNode value = example;
        if (value == null) value = schema.get("example");
        if (value == null) value = schema.get("default");
        if (value == null) return "—";
        return value.isTextual() ? value.asText() : value.toString();
    }

    private void appendSchemaTable(Document document, List<SchemaRow> rows, boolean includeRequired) {
        TableBlock table = new TableBlock();
        TableHead head = new TableHead();
        TableRow header = new TableRow();
        appendCell(header, "参数名称");
        appendCell(header, "参数类型");
        if (includeRequired) appendCell(header, "必填");
        appendCell(header, "说明");
        appendCell(header, "层级");
        appendCell(header, "示例");
        head.appendChild(header);
        table.appendChild(head);
        TableBody body = new TableBody();
        for (SchemaRow row : rows) {
            TableRow tableRow = new TableRow();
            appendCell(tableRow, row.name());
            appendCell(tableRow, row.type());
            if (includeRequired) appendCell(tableRow, row.required() ? "是" : "否");
            appendCell(tableRow, row.description());
            appendCell(tableRow, String.valueOf(row.level()));
            appendCell(tableRow, row.example());
            body.appendChild(tableRow);
        }
        table.appendChild(body);
        document.appendChild(table);
    }

    private void appendBasicInfoTable(Document document, OpenApiReleaseEntity release) {
        TableBlock table = new TableBlock();
        TableHead head = new TableHead();
        TableRow header = new TableRow();
        appendCell(header, "字段");
        appendCell(header, "内容");
        appendCell(header, "字段");
        appendCell(header, "内容");
        head.appendChild(header);
        table.appendChild(head);
        TableBody body = new TableBody();
        appendBasicInfoRow(body, "API 编码", release.getApiNumber(),
                "适用版本", release.getApiVersion());
        appendBasicInfoRow(body, "请求方式", release.getHttpMethod(),
                "发布状态", statusName(release.getStatus()));
        appendBasicInfoRow(body, "代码注册",
                operationRegistry.findByKey(release.getOperationKey()) == null ? "代码缺失" : "已注册",
                "所属模块", release.getDomainName() + " / " + release.getApplicationName()
                        + " / " + release.getFeatureName());
        appendBasicInfoRow(body, "请求 URL", release.getPath(), "", "");
        appendBasicInfoRow(body, "操作标识", release.getOperationKey(), "", "");
        appendBasicInfoRow(body, "用途说明", release.getDescription(), "", "");
        table.appendChild(body);
        document.appendChild(table);
    }

    private void appendBasicInfoRow(TableBody body, String firstLabel, String firstValue,
                                    String secondLabel, String secondValue) {
        TableRow row = new TableRow();
        appendCell(row, firstLabel);
        appendCell(row, valueOrDash(firstValue));
        appendCell(row, secondLabel);
        appendCell(row, secondLabel.isBlank() ? "" : valueOrDash(secondValue));
        body.appendChild(row);
    }

    private void appendCell(TableRow row, String value) {
        TableCell cell = new TableCell();
        cell.appendChild(new Text(value == null ? "" : value));
        row.appendChild(cell);
    }

    private void appendHeading(Document document, int level, String text) {
        Heading heading = new Heading();
        heading.setLevel(level);
        heading.appendChild(new Text(text));
        document.appendChild(heading);
    }

    private void appendJson(Document document, String json) {
        FencedCodeBlock code = new FencedCodeBlock();
        code.setInfo("json");
        code.setLiteral(json);
        document.appendChild(code);
    }

    private String textOrDash(JsonNode node) {
        return valueOrDash(text(node));
    }

    private String text(JsonNode node) {
        if (node == null || node.isNull()) return null;
        return node.isTextual() ? node.asText() : node.toString();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String valueOrEmptyObject(String value) {
        return value == null || value.isBlank() ? "{}" : value;
    }

    private String statusName(String status) {
        if (status == null) return "—";
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "PUBLISHED" -> "已发布";
            case "OFFLINE" -> "已下线";
            default -> valueOrDash(status);
        };
    }

    private record SchemaRow(String name, String type, boolean required,
                             String description, int level, String example) {
    }
}
