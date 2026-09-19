package sm.system.storage;

/** 受控文件用途；统一决定所有存储实现中的稳定对象前缀。 */
public enum FileStoragePurpose {
    ATTACHMENT("attachment"),
    DATA_IMPORT_SOURCE("data-exchange/import/source"),
    DATA_EXPORT_RESULT("data-exchange/export/result"),
    DATA_IMPORT_ERROR("data-exchange/import/error"),
    ONE_TIME_CREDENTIAL("data-exchange/import/credential");

    private final String prefix;

    FileStoragePurpose(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
