package sm.system.json.masking;

/** 敏感数据出站脱敏策略。 */
public enum MaskingType {
    PHONE {
        @Override
        public String mask(String value) {
            if (value == null || value.isEmpty()) return value;
            if (value.length() == 11) return value.substring(0, 3) + "****" + value.substring(7);
            return GENERIC.mask(value);
        }
    },
    EMAIL {
        @Override
        public String mask(String value) {
            if (value == null || value.isEmpty()) return value;
            int atIndex = value.indexOf('@');
            if (atIndex <= 0 || atIndex == value.length() - 1) return GENERIC.mask(value);
            return value.substring(0, 1) + "***" + value.substring(atIndex);
        }
    },
    GENERIC {
        @Override
        public String mask(String value) {
            if (value == null || value.isEmpty()) return value;
            if (value.length() <= 2) return "*".repeat(value.length());
            return value.substring(0, 1) + "*".repeat(value.length() - 2) + value.substring(value.length() - 1);
        }
    },
    REDACT {
        @Override
        public String mask(String value) {
            return value == null ? null : "***";
        }
    };

    public abstract String mask(String value);
}
