package fit.iuh.kttkpm_nhom15_be.users.application.events;

public record UserStatusChangedEvent(String userId, boolean isActive) {}
