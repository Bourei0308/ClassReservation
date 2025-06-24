package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.auth.AccessTokenProvider;

@Service
public class EmailService {

	@Autowired
	private GmailService gmailService;

	@Autowired
	private AccessTokenProvider accessTokenProvider;

	/**
	 * 汎用メール送信メソッド
	 */
	public void sendEmail(String to, String subject, String body) {
		try {
			String accessToken = accessTokenProvider.getAccessToken();
			gmailService.sendEmail(accessToken, to, subject, body);
			System.out.println("✅ メール送信成功：" + to);
		} catch (Exception e) {
			System.err.println("❌ メール送信失敗：" + e.getMessage());
			throw new RuntimeException("メール送信に失敗しました", e);
		}
	}
}
