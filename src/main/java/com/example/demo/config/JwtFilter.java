// com.example.demo.config.JwtFilter.java
package com.example.demo.config;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;

public class JwtFilter implements Filter {

    private final UserRepository userRepo;

    public JwtFilter(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String token = null;

        // 从 Cookie 中读取 token
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("token".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            String userId = JwtUtil.validateToken(token);
            if (userId != null) {
                Optional<User> userOpt = userRepo.findById(userId);
                userOpt.ifPresent(user -> request.setAttribute("currentUser", user));
            }
        }

        chain.doFilter(request, response);
    }
}
