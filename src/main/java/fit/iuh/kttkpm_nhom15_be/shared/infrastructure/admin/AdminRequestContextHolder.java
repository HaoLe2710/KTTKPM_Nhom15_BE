package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin;

public final class AdminRequestContextHolder {

  private static final ThreadLocal<String> ROLE_HOLDER = new ThreadLocal<>();
  private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

  private AdminRequestContextHolder() {
  }

  public static void set(String role, String userId) {
    ROLE_HOLDER.set(role);
    USER_ID_HOLDER.set(userId);
  }

  public static String currentRole() {
    return ROLE_HOLDER.get();
  }

  public static String currentUserId() {
    return USER_ID_HOLDER.get();
  }

  public static void clear() {
    ROLE_HOLDER.remove();
    USER_ID_HOLDER.remove();
  }
}
