package com.example.pbc.security;

import com.example.pbc.rest_controller.ScoreController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

//@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromHeader(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String uuid = jwtUtil.extractUuid(token);
            log.info("Аутентификация успешна для UUID: {}", uuid);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    uuid, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Токен не валиден или отсутствует");
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}