package com.example.demo.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus; // ✅ SpringのHttpStatusを使う
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.AccessTokenProvider;

@RestController
@RequestMapping("/api/token")
public class TokenController {

	private final AccessTokenProvider accessTokenProvider;

	public TokenController(AccessTokenProvider accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
	}

	//アクセストークンを自動取得
	@GetMapping("/get")
	public ResponseEntity<String> getToken() {
		try {
			String token = accessTokenProvider.getAccessToken();
			return ResponseEntity.ok(token);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("トークン取得失敗: " + e.getMessage());
		}
	}
}
