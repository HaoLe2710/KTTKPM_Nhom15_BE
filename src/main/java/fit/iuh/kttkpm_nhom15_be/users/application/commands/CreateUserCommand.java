package fit.iuh.kttkpm_nhom15_be.users.application.commands;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
public record CreateUserCommand(String email, String phone, String password, String fullName, UserRole role) {}
