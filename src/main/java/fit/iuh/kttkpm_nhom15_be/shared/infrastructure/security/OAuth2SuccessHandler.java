package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security;

import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserFacade userFacade;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userFacade.findByIdentifier(email).orElseGet(() -> {
            // Tự động Sign-in nếu lần đầu đăng nhập Google
            User newUser = User.builder()
                    .email(email)
                    .fullName(name)
                    .role(UserRole.CUSTOMER)
                    .isActive(true)
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtProvider.generateToken(user.getEmail());
        response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
    }
}