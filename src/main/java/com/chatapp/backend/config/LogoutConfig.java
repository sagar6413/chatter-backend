package com.chatapp.backend.config;

import com.chatapp.backend.config.jwt.JwtService;
import com.chatapp.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class LogoutConfig implements LogoutHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void logout(@NotNull HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwtToken;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        jwtToken = authHeader.substring(7);
        var username = jwtService.extractUsername(jwtToken);
        var user = this.userRepository.findByUsername(username).get();
        userRepository.updateRefreshToken(user.getId(), null);
    }
}