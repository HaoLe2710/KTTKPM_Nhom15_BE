package fit.iuh.kttkpm_nhom15_be.auth.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.dto.RegisterRequest;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {
    private final UserFacade userFacade;

    public void execute(RegisterRequest request) {
        userFacade.registerNewUser(request);
    }
}