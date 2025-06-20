package com.example.demo.auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;

@Component
public class AccessTokenProvider {

	@Value("${google.client.id}")
	private String clientId;

	@Value("${google.client.secret}")
	private String clientSecret;

	@Value("${google.refresh.token}")
	private String refreshToken;

	public String getAccessToken() throws IOException {
		try {
			UserCredentials credentials = UserCredentials.newBuilder()
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setRefreshToken(refreshToken)
					.build();

			AccessToken token = credentials.refreshAccessToken();

			if (token == null || token.getTokenValue() == null) {
				throw new IOException("アクセストークンの取得に失敗しました（token == null）");
			}

			// オプション：トークンの有効期限をログに出す（確認用）
			System.out.println("✅ アクセストークン取得成功（有効期限: " + token.getExpirationTime() + "）");

			return token.getTokenValue();

		} catch (IOException e) {
			System.err.println("❌ アクセストークン取得エラー: " + e.getMessage());
			System.err.println("📌 clientId: " + clientId);
			throw e;
		}
	}
}
