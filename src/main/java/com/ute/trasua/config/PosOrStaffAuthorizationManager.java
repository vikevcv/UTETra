package com.ute.trasua.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Allows access when the caller has a staff role (ADMIN/MANAGER/STAFF)
 * OR when a POS kiosk session (POS_ACCOUNT) is present.
 */
@Component
public class PosOrStaffAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            boolean staff = auth.getAuthorities().stream().anyMatch(a -> {
                String name = a.getAuthority() == null ? "" : a.getAuthority().toUpperCase();
                return name.contains("ADMIN") || name.contains("MANAGER") || name.contains("STAFF");
            });
            if (staff) {
                return new AuthorizationDecision(true);
            }
        }
        HttpServletRequest request = context.getRequest();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("POS_ACCOUNT") != null) {
                return new AuthorizationDecision(true);
            }
        }
        return new AuthorizationDecision(false);
    }
}
