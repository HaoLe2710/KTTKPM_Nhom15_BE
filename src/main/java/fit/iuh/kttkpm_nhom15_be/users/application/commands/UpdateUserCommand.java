package fit.iuh.kttkpm_nhom15_be.users.application.commands;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
public record UpdateUserCommand(String id, String email, String phone, String fullName, UserRole role) {}
