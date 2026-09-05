package sm.domain.sys.base.openapi.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.BulletList;
import org.commonmark.node.Document;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.ListItem;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
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

    public OpenApiMarkdownDocumentGenerator(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public byte[] generate(List<OpenApiReleaseEntity> releases) {
        Document document = new Document();
        appendHeading(document, 1, "API 接口文档");
        for (OpenApiReleaseEntity release : releases) {
            appendHeading(document, 2, release.getName());
            appendHeading(document, 3, "接口基本信息");
            BulletList metadata = new BulletList();
            appendListItem(metadata, "用途说明：" + valueOrDash(release.getDescription()));
            appendListItem(metadata, "请求方式：" + release.getHttpMethod());
            appendListItem(metadata, "请求 URL：" + release.getPath());
            appendListItem(metadata, "API 编码：" + release.getApiNumber());
            appendListItem(metadata, "操作标识：" + release.getOperationKey());
            appendListItem(metadata, "所属模块：" + release.getDomainName() + " / "
                    + release.getApplicationName() + " / " + release.getFeatureName());
            appendListItem(metadata, "适用版本：" + release.getApiVersion() + "（"
                    + statusName(release.getStatus()) + "）");
            document.appendChild(metadata);
            if (release.getDocumentation() != null && !release.getDocumentation().isBlank()) {
                appendParagraph(document, release.getDocumentation());
            }

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
            // 历史元数据异常时仍允许导出其他接口，参数表明确标记无法解析。
            return List.of(new SchemaRow("—", "—", false, "JSON Schema 无法解析", 1, "—"));
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
            JsonNode nestedSchema = "array".equals(text(property.get("type"))) ? property.get("items") : property;
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

    private void appendCell(TableRow row, String value) {
        TableCell cell = new TableCell();
        cell.appendChild(new Text(valueOrDash(value)));
        row.appendChild(cell);
    }

    private void appendHeading(Document document, int level, String text) {
        Heading heading = new Heading();
        heading.setLevel(level);
        heading.appendChild(new Text(text));
        document.appendChild(heading);
    }

    private void appendParagraph(Document document, String text) {
        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text(valueOrDash(text)));
        document.appendChild(paragraph);
    }

    private void appendListItem(BulletList list, String text) {
        ListItem item = new ListItem();
        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text(text));
        item.appendChild(paragraph);
        list.appendChild(item);
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
        return node == null || node.isNull() ? null : node.asText();
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
