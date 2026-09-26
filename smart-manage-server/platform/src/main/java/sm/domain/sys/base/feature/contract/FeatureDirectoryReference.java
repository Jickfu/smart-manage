package sm.domain.sys.base.feature.contract;

/** 功能的真实目录归属，仅用于展示和分组，不构成授权结果。 */
public record FeatureDirectoryReference(String featureKey, Long domainId, String domainName,
                                        Long appId, String appName) { }
