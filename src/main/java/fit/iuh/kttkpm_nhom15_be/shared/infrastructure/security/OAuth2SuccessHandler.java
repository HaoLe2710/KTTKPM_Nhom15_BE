package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security;

import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import jakarta.servlet.http.Cookie;
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

    private final JwtProvider jwtProvider;
    private final UserFacade userFacade;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 1. Tìm hoặc tạo User mới
        User user = userFacade.findByIdentifier(email).orElseGet(() ->
                userFacade.createOAuth2User(email, name)
        );

        // 2. Tạo JWT token
        String token = jwtProvider.generateToken(user.getEmail());

        // 3. Tạo HttpOnly Cookie để giấu Token
        Cookie authCookie = new Cookie("AUTH-TOKEN", token);
        authCookie.setHttpOnly(true);       // Chống XSS (JS không đọc được)
        authCookie.setSecure(false);         //
        authCookie.setPath("/");             // Có tác dụng toàn trang
        authCookie.setMaxAge(7 * 24 * 60 * 60); // Sống 7 ngày

        // 4. Thêm Cookie vào Response
        response.addCookie(authCookie);

        // 5. Redirect về Front-end
        String targetUrl = "http://localhost:3000/oauth2/redirect";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}