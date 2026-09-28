package sm.system.bill;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 单据状态目录与纯状态转换策略。
 *
 * <p>本类不访问数据库、不处理权限，也不代替业务事务。业务 TxService 必须使用数据库当前状态和并发版本
 * 调用本策略，再以状态和版本作为原子更新条件。
 */
public final class BillStatusPolicy {
    private final String initialStatus;
    private final Map<String, BillStatusDefinition> definitions;
    private final Set<String> editableStatuses;
    private final Map<TransitionKey, String> transitions;

    private BillStatusPolicy(Builder builder) {
        this.initialStatus = builder.initialStatus;
        // 状态声明顺序会用于稳定生成筛选项和汇总结果，不能依赖 Map.copyOf 的未定义迭代顺序。
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.definitions));
        this.editableStatuses = Set.copyOf(builder.editableStatuses);
        this.transitions = Map.copyOf(builder.transitions);
    }

    /** 标准策略：A 为初始及可编辑状态，提交 A→B，审核 B→C。 */
    public static BillStatusPolicy standard() {
        return standardBuilder().build();
    }

    /** 业务聚合在标准状态基础上增加自身状态和转换。 */
    public static Builder standardBuilder() {
        Builder builder = new Builder();
        for (StandardBillStatus status : StandardBillStatus.values()) {
            builder.addStatus(status.definition());
        }
        return builder
                .initial(StandardBillStatus.SAVED.getValue())
                .editable(StandardBillStatus.SAVED.getValue())
                .transition(StandardBillStatusAction.SUBMIT.name(),
                        StandardBillStatus.SAVED.getValue(), StandardBillStatus.SUBMITTED.getValue())
                .transition(StandardBillStatusAction.AUDIT.name(),
                        StandardBillStatus.SUBMITTED.getValue(), StandardBillStatus.AUDITED.getValue());
    }

    public String initialStatus() {
        return initialStatus;
    }

    public List<BillStatusDefinition> definitions() {
        return List.copyOf(definitions.values());
    }

    public BillStatusDefinition requireKnown(String status) {
        BillStatusDefinition definition = definitions.get(status);
        if (definition == null) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "未知单据状态：" + status);
        }
        return definition;
    }

    public boolean isEditable(String status) {
        requireKnown(status);
        return editableStatuses.contains(status);
    }

    public void requireEditable(String status) {
        if (!isEditable(status)) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "当前单据状态不允许普通保存");
        }
    }

    public String transition(StandardBillStatusAction action, String currentStatus) {
        return transition(action.name(), currentStatus);
    }

    public String transition(String action, String currentStatus) {
        requireKnown(currentStatus);
        String targetStatus = transitions.get(new TransitionKey(requireAction(action), currentStatus));
        if (targetStatus == null) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR,
                    "当前单据状态不允许执行动作：" + action);
        }
        return targetStatus;
    }

    public static final class Builder {
        private String initialStatus;
        private final Map<String, BillStatusDefinition> definitions = new LinkedHashMap<>();
        private final Set<String> editableStatuses = new LinkedHashSet<>();
        private final Map<TransitionKey, String> transitions = new LinkedHashMap<>();

        public Builder addStatus(String value, String name) {
            return addStatus(new BillStatusDefinition(value, name));
        }

        public Builder addStatus(BillStatusDefinition definition) {
            if (definitions.putIfAbsent(definition.value(), definition) != null) {
                throw new IllegalArgumentException("单据状态编码重复：" + definition.value());
            }
            return this;
        }

        public Builder initial(String status) {
            this.initialStatus = status;
            return this;
        }

        public Builder editable(String... statuses) {
            editableStatuses.addAll(List.of(statuses));
            return this;
        }

        public Builder transition(StandardBillStatusAction action, String fromStatus, String toStatus) {
            return transition(action.name(), fromStatus, toStatus);
        }

        public Builder transition(String action, String fromStatus, String toStatus) {
            TransitionKey key = new TransitionKey(requireAction(action), fromStatus);
            if (transitions.putIfAbsent(key, toStatus) != null) {
                throw new IllegalArgumentException("单据状态转换重复：" + action + " / " + fromStatus);
            }
            return this;
        }

        public BillStatusPolicy build() {
            requireConfiguredStatus(initialStatus, "初始状态");
            for (String editableStatus : editableStatuses) {
                requireConfiguredStatus(editableStatus, "可编辑状态");
            }
            for (Map.Entry<TransitionKey, String> transition : new ArrayList<>(transitions.entrySet())) {
                requireConfiguredStatus(transition.getKey().fromStatus(), "转换来源状态");
                requireConfiguredStatus(transition.getValue(), "转换目标状态");
            }
            return new BillStatusPolicy(this);
        }

        private void requireConfiguredStatus(String status, String usage) {
            if (!definitions.containsKey(status)) {
                throw new IllegalArgumentException(usage + "未在状态目录中定义：" + status);
            }
        }
    }

    private static String requireAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("单据状态动作不能为空");
        }
        return action.trim();
    }

    private record TransitionKey(String action, String fromStatus) {
    }
}
