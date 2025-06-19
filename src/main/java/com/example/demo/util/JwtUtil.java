// com.example.demo.util.JwtUtil.java
package com.example.demo.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
	private static final Key SECRET_KEY = Keys.hmacShaKeyFor("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24小时

    public static String generateToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact();
    }

    public static String validateToken(String token) {
        try {
        	Claims claims = Jwts.parserBuilder()
        		    .setSigningKey(SECRET_KEY)  // SECRET_KEY 是 Key 类型
        		    .build()
        		    .parseClaimsJws(token)
        		    .getBody();
        	
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
