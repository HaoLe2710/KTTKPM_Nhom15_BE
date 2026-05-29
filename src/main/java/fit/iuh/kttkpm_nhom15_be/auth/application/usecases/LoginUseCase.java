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
                .orElseThrow(() -> new InvalidCredentialsException("Tài khoản hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Tài khoản hoặc mật khẩu không chính xác");
        }

        if (!user.isActive()) {
            throw new AccountInactiveException("Tài khoản đã bị khóa");
        }

        return jwtProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());
    }
}
