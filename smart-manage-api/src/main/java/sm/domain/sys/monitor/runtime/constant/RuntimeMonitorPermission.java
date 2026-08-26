package sm.domain.sys.monitor.runtime.constant;

/** 运行监控权限码，统一供接口鉴权和权限审计使用。 */
public final class RuntimeMonitorPermission {
  public static final String VIEW = "sys:monitor:runtime:view";
  public static final String MANAGE = "sys:monitor:runtime:manage";

  private RuntimeMonitorPermission() {}
}
