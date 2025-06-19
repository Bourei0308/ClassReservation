package com.example.demo.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 登录接口
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Optional<User> userOpt = userRepo.findAll().stream()
            .filter(u -> u.getAccount().equals(loginRequest.getAccount()))
            .filter(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
            .findFirst();

        if (!userOpt.isPresent()) return null;

        // 生成 JWT 并写入 Cookie
        String token = JwtUtil.generateToken(userOpt.get().getId());
     // 使用 ResponseCookie 设置持久化 Cookie（例如 7 天）
        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
            .httpOnly(true)
            .secure(false) // 如果是 HTTPS 建议设置为 true
            .path("/")
            .maxAge(7*24*60*60) // 7 天
            .sameSite("Lax")
            .build();
        
        response.setHeader("Set-Cookie", cookie.toString());

        return userOpt.get();
    }
    
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
            .httpOnly(true)
            .path("/")
            .maxAge(0) // 立即删除
            .sameSite("Lax")
            .build();
        response.setHeader("Set-Cookie", cookie.toString());
    }
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@CookieValue(name = "auth_token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String userId = JwtUtil.validateToken(token); // 你实现的 JwtUtil
            Optional<User> userOpt = userRepo.findById(userId);
            return userOpt.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // DTO 用于接收请求体
    static class LoginRequest {
        private String account;
        private String password;

        public String getAccount() { return account; }
        public void setAccount(String account) { this.account = account; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
