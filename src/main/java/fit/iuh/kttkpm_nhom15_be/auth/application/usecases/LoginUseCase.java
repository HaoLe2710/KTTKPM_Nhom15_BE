package fit.iuh.kttkpm_nhom15_be.auth.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.AccountInactiveException;
import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.InvalidCredentialsException;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {
    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public String execute(String identifier, String password) {
        User user = userFacade.findByIdentifier(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("Tai khoan hoac mat khau khong chinh xac"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Tai khoan hoac mat khau khong chinh xac");
        }

        if (!user.isActive()) {
            throw new AccountInactiveException("Tai khoan da bi khoa");
        }

        return jwtProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());
    }
}
