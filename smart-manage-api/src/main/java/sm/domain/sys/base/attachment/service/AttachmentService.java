package sm.domain.sys.base.attachment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.system.aop.log.BizLog;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.system.storage.FileStorageServiceFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 附件服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentMapper mapper;
    private final BizAttachmentMapper bizMapper;
    private final FileStorageServiceFactory storageFactory;
    private final AttachmentTxService txService;

    /** 上传附件：传 bizType 时存入临时目录（需 promote），否则直接存 sys 系统目录 */
    @BizLog(value = "上传附件", saveRequest = false)
    public AttachmentVO upload(MultipartFile file, String bizType) throws IOException {
        return txService.upload(file, bizType);
    }

    /** 提升附件：关联业务单据 + 移出临时目录 */
    @BizLog("确认附件")
    public void promote(AttachmentPromoteForm form) throws IOException {
        txService.promote(form);
    }

    /** 删除附件（物理文件 + 映射 + 元数据） */
    @BizLog("删除附件")
    public void delete(Long id) throws IOException {
        txService.delete(id);
    }

    /** 按业务单据查询附件列表 */
    public List<AttachmentVO> listByBiz(String bizType, String bizId) {
        List<AttachmentEntity> entities = mapper.selectByBiz(bizType, bizId);
        return entities.stream().map(this::assembleAttachmentVO).collect(Collectors.toList());
    }

    /** 列出现有附件,无论是临时还是正式都有 */
    public List<AttachmentVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(ids).stream().map(this::assembleAttachmentVO).collect(Collectors.toList());
    }

    public AttachmentEntity getById(Long id) {
        return mapper.selectById(id);
    }

    /** 访问地址依赖当前存储实现，因此属于业务组装而非纯字段映射。 */
    private AttachmentVO assembleAttachmentVO(AttachmentEntity entity) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(entity.getId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setFileSize(entity.getFileSize());
        vo.setMimeType(entity.getMimeType());
        vo.setFileExt(entity.getFileExt());
        vo.setIsTemp(entity.getIsTemp());
        vo.setUrl(storageFactory.getService().getAccessUrl(entity.getStoredPath()));
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return vo;
    }
}
