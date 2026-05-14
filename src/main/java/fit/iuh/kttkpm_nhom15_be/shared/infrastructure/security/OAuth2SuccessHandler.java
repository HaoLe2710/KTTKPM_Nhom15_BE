package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security;

import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserFacade userFacade;

    @Value("${app.oauth2.redirect-default-url:http://localhost:5173/oauth2/redirect}")
    private String defaultRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userFacade.findByIdentifier(email).orElseGet(() ->
                userFacade.createOAuth2User(email, name)
        );

        String token = jwtProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        Cookie authCookie = new Cookie("AUTH-TOKEN", token);
        authCookie.setHttpOnly(true);
        authCookie.setSecure(false);
        authCookie.setPath("/");
        authCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(authCookie);

        String targetUrl = resolveRedirectUrl(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveRedirectUrl(HttpServletRequest request) {
        String origin = extractOriginFromHeader(request.getHeader("Origin"));
        if (origin == null) {
            origin = extractOriginFromHeader(request.getHeader("Referer"));
        }

        if (origin != null && isAllowedFrontendOrigin(origin)) {
            return origin + "/oauth2/redirect";
        }
        return defaultRedirectUrl;
    }

    private String extractOriginFromHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(headerValue.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            return port > 0 ? scheme + "://" + uri.getHost() + ":" + port : scheme + "://" + uri.getHost();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isAllowedFrontendOrigin(String origin) {
        List<String> prefixes = List.of(
                "http://localhost:",
                "http://127.0.0.1:",
                "http://192.168.",
                "http://10."
        );
        return prefixes.stream().anyMatch(origin::startsWith);
    }
}
