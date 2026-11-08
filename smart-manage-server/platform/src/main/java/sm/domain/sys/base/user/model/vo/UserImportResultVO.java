package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import java.util.List;

@Schema(description = "用户导入结果输出模型")
public record UserImportResultVO(
    @Schema(description = "本次导入的数据行总数")
    int total,
    @Schema(description = "成功导入数量")
    int success,
    @Schema(description = "导入失败数量")
    int failed,
    @Schema(description = "导入错误说明列表")
    List<String> errors,
    @Schema(description = "导入警告说明列表")
    List<String> warnings,
    @Schema(description = "导入生成的凭据文件下载信息")
    List<FileArtifactReference> credentialFiles,
    @Schema(description = "导入错误文件下载信息")
    FileArtifactReference errorFile) { }
