package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ShopperAccessGuard {

    public String resolveAllowedUserId(String requestedUserId) {
        if (requestedUserId == null || requestedUserId.isBlank()) {
            throw new AccessDeniedException("userId khong hop le");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof String principalUserId && !"anonymousUser".equals(principalUserId)) {
            if (!principalUserId.equals(requestedUserId)) {
                throw new AccessDeniedException("userId khong khop voi tai khoan dang nhap");
            }
            return principalUserId;
        }

        if (!requestedUserId.startsWith("guest-")) {
            throw new AccessDeniedException("Khach mua hang phai su dung guest userId hop le");
        }

        return requestedUserId;
    }
}
