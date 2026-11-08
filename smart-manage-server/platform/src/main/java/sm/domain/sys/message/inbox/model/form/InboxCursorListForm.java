package sm.domain.sys.message.inbox.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import jakarta.validation.constraints.Pattern;

/**
 * 当前用户消息游标查询。
 */
@Schema(description = "站内消息收件箱游标查询输入表单")
@Data
public class InboxCursorListForm {
    @Schema(description = "每页记录数")
    private Integer pageSize = 20;
    /**
     * 与普通列表共用过滤协议，字段由服务端白名单限定
     */
    @Schema(description = "受控列表筛选 JSON；允许的字段和操作符由所属 Service 注册")
    private String filters;
    @Schema(description = "是否仅查询未读消息")
    private Boolean unreadOnly = false;
    @Schema(description = "是否仅查询当前月份的消息")
    private Boolean monthOnly = false;
    /**
     * 分类直接映射既有收件范围，不以标题或场景名称推断
     */
    @Schema(description = "收件范围：ALL_ENABLED_USERS 全部启用用户、USERS 指定用户")
    @Pattern(regexp = "ALL_ENABLED_USERS|USERS", message = "消息类型不正确")
    private String audienceType;
    @Schema(description = "上一页末条消息接收时间，与 cursorMessageId 配对使用")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}",
            message = "消息游标时间格式不正确")
    private String cursorTime;
    @Schema(description = "上一页末条消息 ID，与 cursorTime 配对使用")
    private Long cursorMessageId;

    public int safePageSize() {
        if (pageSize == null || pageSize < 1) return 20;
        return Math.min(pageSize, 100);
    }
}
