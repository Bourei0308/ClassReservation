package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Vueポートを明示
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	// 初始化 demo 数据
	@PostMapping("/init")
	@Operation(summary = "（テスト）ユーザデータ初期化")
	public String initDemoUsers() {
		userRepository.deleteAll();

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		userRepository.save(new User("0", "admin", "2430371802@qq.com", encoder.encode("admin123"), 0, "admin123"));
		userRepository
				.save(new User("1", "student", null, encoder.encode("student123"), 1, "student123"));
		userRepository
				.save(new User("2", "teacher", null, encoder.encode("teacher123"), 2, "teacher123"));

		return "ユーザデータ初期化しました";
	}

	@GetMapping("/email/{email}")
	public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
		String decodedEmail = java.net.URLDecoder.decode(email, java.nio.charset.StandardCharsets.UTF_8);
		return userRepository.findByEmail(decodedEmail)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@GetMapping("")
	@Operation(summary = "ユーザ一覧取得")
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@PostMapping("/register")
	@Operation(summary = "ユーザ作成")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		boolean accountExists = userRepository.findAll().stream()
				.anyMatch(u -> u.getAccount().equals(user.getAccount()));
		boolean nameExists = userRepository.findAll().stream()
				.anyMatch(u -> u.getName().equals(user.getName()));

		if (accountExists) {
			return ResponseEntity.badRequest().body("account-exists");
		}
		if (nameExists) {
			return ResponseEntity.badRequest().body("name-exists");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		User savedUser = userRepository.save(user);
		return ResponseEntity.ok(savedUser);
	}

	@GetMapping("/{id}")
	@Operation(summary = "ユーザーをIDで取得")
	public User getUserById(@PathVariable("id") String id) {
		return userRepository.findById(id).orElse(null);
	}

	@PutMapping("/{id}")
	@Operation(summary = "ユーザ情報更新（パスワード以外）")
	public ResponseEntity<?> updateUser(@PathVariable("id") String id, @RequestBody User user) {
		user.setId(id);

		boolean nameExists = userRepository.findAll().stream()
				.anyMatch(u -> !u.getId().equals(id) && u.getName().equals(user.getName()));

		if (nameExists) {
			return ResponseEntity.badRequest().body("name-exists");
		}

		return ResponseEntity.ok(userRepository.save(user));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "ユーザ削除")
	public String deleteUser(@PathVariable("id") String id) {
		userRepository.deleteById(id);
		return "ユーザを削除しました";
	}

	@PutMapping("/{id}/password")
	@Operation(summary = "パスワード変更（旧パスワード不要）")
	public ResponseEntity<?> updatePassword(@PathVariable("id") String id, @RequestBody NewPasswordRequest request) {
		Optional<User> optionalUser = userRepository.findById(id);
		if (!optionalUser.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user-not-found");
		}

		User user = optionalUser.get();
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		return ResponseEntity.ok("password-updated");
	}

	//パスワード変更用DTO
	static class NewPasswordRequest {
		private String newPassword;

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}
	}

	//
	@GetMapping("/students")
	@Operation(summary = "生徒一覧（role=1）の取得")
	public List<User> getStudents() {
		return userRepository.findByRole(1); // 1 = 生徒
	}
	
	@PutMapping("/{id}/email")
	@Operation(summary = "ユーザのメールアドレス更新")
	public ResponseEntity<?> updateEmail(@PathVariable("id") String id, @RequestBody Map<String, String> payload) {
	    Optional<User> optionalUser = userRepository.findById(id);
	    if (!optionalUser.isPresent()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user-not-found");
	    }

	    String newEmail = payload.get("email");
	    if (newEmail == null || newEmail.trim().isEmpty()) {
	        return ResponseEntity.badRequest().body("email-required");
	    }

	    User user = optionalUser.get();
	    user.setEmail(newEmail.trim());
	    userRepository.save(user);

	    return ResponseEntity.ok("email-updated");
	}
	
	@PostMapping("/bulk")
	@Operation(summary = "ユーザ一括登録")
	public ResponseEntity<?> registerUsersBulk(@RequestBody List<User> users) {
	    for (User user : users) {
	        if (user.getAccount() == null || user.getAccount().isEmpty()
	            || user.getName() == null || user.getName().isEmpty()
	            || user.getPassword() == null || user.getPassword().isEmpty()
	            || user.getRole() == 0) {
	            return ResponseEntity.badRequest().body("invalid-user-data");
	        }

	        boolean accountExists = userRepository.findAll().stream()
	                .anyMatch(u -> u.getAccount().equals(user.getAccount()));
	        boolean nameExists = userRepository.findAll().stream()
	                .anyMatch(u -> u.getName().equals(user.getName()));
	        if (accountExists || nameExists) {
	            return ResponseEntity.badRequest().body("duplicate-account-or-name");
	        }

	        user.setPassword(passwordEncoder.encode(user.getPassword()));
	    }

	    userRepository.saveAll(users);
	    return ResponseEntity.ok("users-registered");
	}


}
