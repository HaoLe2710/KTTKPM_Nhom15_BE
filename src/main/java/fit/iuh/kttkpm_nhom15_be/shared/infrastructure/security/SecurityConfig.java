package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - Vui lòng đăng nhập"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agent/message").permitAll()
                        .requestMatchers("/api/v1/payments/vnpay-return", "/api/v1/payments/vnpay-ipn", "/api/v1/payments/sepay-webhook").permitAll()
                        .requestMatchers("/api/v1/files/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-ui/**", "/docs/api/**").permitAll()
                        .requestMatchers("/api/v1/products", "/api/v1/products/*").permitAll()
                        .requestMatchers("/api/v1/products/search", "/api/v1/search/suggestions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews").permitAll()
                        .requestMatchers("/api/v1/product-types", "/api/v1/options").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/carts/items").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/carts/items/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/items/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/active").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/shipping-fee/quote").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/promotions/active").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/create").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/order/*").permitAll()
                        .requestMatchers("/login/**", "/oauth2/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*",
                "http://10.*:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/ws/**", configuration);
        source.registerCorsConfiguration("/auth/ws/**", configuration);
        return source;
    }
}
