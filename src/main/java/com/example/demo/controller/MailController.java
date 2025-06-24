
package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.AccessTokenProvider;
import com.example.demo.entity.MailRequest;
import com.example.demo.service.GmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/mail")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gmail API", description = "Gmail送信API")
public class MailController {
	@Autowired
	private GmailService gmailService;
	@Autowired
	private AccessTokenProvider accessTokenProvider;

	@PostMapping("/send")
	@Operation(summary = "Gmailでメール送信（Vueのaccess_token使用）")
	public ResponseEntity<String> sendMail(
			@RequestBody MailRequest mailRequest) {
		try {
			String accessToken = accessTokenProvider.getAccessToken();
			gmailService.sendEmail(accessToken,
					mailRequest.getTo(),
					mailRequest.getSubject(),
					mailRequest.getBody());

			return ResponseEntity.ok("メール送信に成功しました。");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("送信エラー: " + e.getMessage());
		}
	}
}
