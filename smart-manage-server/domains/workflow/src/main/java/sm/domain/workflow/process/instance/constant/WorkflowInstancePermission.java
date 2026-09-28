package sm.domain.workflow.process.instance.constant;

public final class WorkflowInstancePermission {
    public static final String LIST = "workflow:process:instance:listPage";
    public static final String DETAIL = "workflow:process:instance:detail";
    public static final String SUSPEND = "workflow:process:instance:suspend";
    public static final String RESUME = "workflow:process:instance:resume";
    public static final String TERMINATE = "workflow:process:instance:terminate";
    public static final String JUMP = "workflow:process:instance:jump";
    public static final String VARIABLES = "workflow:process:instance:variables";
    public static final String SCRIPT_RETRY = "workflow:process:instance:script-retry";
    private WorkflowInstancePermission() { }
}
