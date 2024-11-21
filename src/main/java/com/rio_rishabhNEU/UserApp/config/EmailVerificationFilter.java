package com.rio_rishabhNEU.UserApp.filter;

import com.rio_rishabhNEU.UserApp.Service.UserService;
import com.rio_rishabhNEU.UserApp.util.AuthUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class EmailVerificationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationFilter.class);
    private final UserService userService;

    public EmailVerificationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = AuthUtil.getAuthenticatedUserEmail();
        if (email != null && !userService.isUserVerified(email)) {
            logger.warn("Unverified user attempting to access protected resource: {}", email);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Email not verified");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return path.equals("/healthz") ||
                path.equals("/v1/user") ||
                path.startsWith("/v1/verifyEmail");
    }
}