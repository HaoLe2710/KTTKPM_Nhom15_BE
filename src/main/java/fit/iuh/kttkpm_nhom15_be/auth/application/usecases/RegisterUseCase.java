package fit.iuh.kttkpm_nhom15_be.auth.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.CreateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.CreateUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {
    private final CreateUserUseCase createUserUseCase;

    public void execute(String email, String phone, String password, String fullName) {
        CreateUserCommand command = new CreateUserCommand(email, phone, password, fullName, UserRole.CUSTOMER);
        createUserUseCase.execute(command);
    }
}