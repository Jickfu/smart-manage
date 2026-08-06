package sm.domain.sys.base.fileconfig.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.domain.sys.base.fileconfig.model.vo.FileConfigDetailVO;
import sm.framework.mapping.SmMapperConfig;

/** 文件配置对外模型转换器，敏感密文不进入 VO。 */
@Mapper(config = SmMapperConfig.class)
interface FileConfigConverter {
    @Mapping(target = "ftpPasswordConfigured", expression = "java(entity.getFtpPasswordCipher() != null)")
    @Mapping(target = "s3SecretKeyConfigured", expression = "java(entity.getS3SecretKeyCipher() != null)")
    FileConfigDetailVO toDetailVO(FileConfigEntity entity);
}
