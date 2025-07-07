
package com.example.demo.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
import com.example.demo.service.EmailService;
import com.example.demo.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private EmailService emailService; // ✅ メール送信サービス（別途作成が必要）

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	// ログイン
	@PostMapping("/login")
	public User login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
		Optional<User> userOpt = userRepo.findAll().stream()
				.filter(u -> u.getAccount().equals(loginRequest.getAccount()))
				.filter(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
				.findFirst();

		if (!userOpt.isPresent())
			return null;

		String token = JwtUtil.generateToken(userOpt.get().getId());

		ResponseCookie cookie = ResponseCookie.from("auth_token", token)
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(7 * 24 * 60 * 60)
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
				.maxAge(0)
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
			String userId = JwtUtil.validateToken(token);
			Optional<User> userOpt = userRepo.findById(userId);
			return userOpt.map(ResponseEntity::ok)
					.orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	// 認証コード送信（パスワード変更前に呼ばれる）
	@PostMapping("/send-code")
	public ResponseEntity<?> sendCode(@RequestBody Map<String, String> payload, HttpSession session) {
		String email = payload.get("email");
		if (email == null || email.isBlank()) {
			return ResponseEntity.badRequest().body("メールアドレスが必要です");
		}

		// 6桁認証コード生成
		String code = String.format("%06d", new Random().nextInt(999999));

		// セッションに保存（有効期限用のタイムスタンプも）
		session.setAttribute("verificationCode", code);
		session.setAttribute("codeTimestamp", System.currentTimeMillis());
		session.setAttribute("resetEmail", email);

		// メール送信
		String subject = "【じゅくポン】パスワード変更用認証コード";
		String body = "以下の6桁認証コードを入力してください：\n\n認証コード: " + code + "\n\n※このコードの有効期限は10分です。";
		emailService.sendEmail(email, subject, body);

		return ResponseEntity.ok().build();
	}

	// 認証コード検証
	@PostMapping("/verify-code")
	public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload, HttpSession session) {
		String inputCode = payload.get("code");
		String savedCode = (String) session.getAttribute("verificationCode");
		Long timestamp = (Long) session.getAttribute("codeTimestamp");

		if (savedCode == null || timestamp == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("認証コードが未送信または期限切れです");
		}

		if (!inputCode.equals(savedCode)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("認証コードが一致しません");
		}

		// 10分の有効期限確認
		long now = System.currentTimeMillis();
		if (now - timestamp > 10 * 60 * 1000) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("認証コードの有効期限が切れています");
		}

		// 認証成功 → セッションから削除
		session.removeAttribute("verificationCode");
		session.removeAttribute("codeTimestamp");
		session.setAttribute("passwordResetVerified", true); // ✅ 追加

		return ResponseEntity.ok().build();
	}

	// AuthController.java
	@PostMapping("/change-password")
	public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
		Optional<User> optionalUser = userRepo.findByEmailAndName(request.getEmail(), request.getName());
		if (optionalUser.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ユーザーが見つかりません");
		}

		User user = optionalUser.get();
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepo.save(user);

		return ResponseEntity.ok("パスワード変更完了");
	}

	// DTO
	static class LoginRequest {
		private String account;
		private String password;

		public String getAccount() {
			return account;
		}

		public void setAccount(String account) {
			this.account = account;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	// DTO（ログイン情報に続けて追加でOK）
	static class ChangePasswordRequest {
		private String email;
		private String name;
		private String newPassword;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}
	}

}
