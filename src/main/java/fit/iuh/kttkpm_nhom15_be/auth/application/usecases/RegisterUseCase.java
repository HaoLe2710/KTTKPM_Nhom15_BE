package fit.iuh.kttkpm_nhom15_be.auth.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {
    private final UserFacade userFacade;

    public void execute(String email, String phone, String password, String fullName) {
        // Gọi qua Facade để đảm bảo tính đóng gói (Encapsulation)
        userFacade.registerCustomer(email, phone, password, fullName);
    }
}