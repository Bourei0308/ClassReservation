package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification API", description = "お知らせAPI")
public class NotificationController {
	@Autowired
	private NotificationRepository repository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/user/{userId}")
	@Operation(summary = "全てのお知らせ取得")
	public List<Notification> getByUser(@PathVariable String userId) {
		return repository.findByUserId(userId);
	}

	@PostMapping
	@Operation(summary = "お知らせの追加とメール送信")
	public Notification create(@RequestBody Notification notification) {
		notification.setCreatedAt(LocalDateTime.now());
		notification.setRead(false);
		Notification savedNotification = repository.save(notification);

		// メール送信処理
		try {
			String to = getEmailByUserId(notification.getUserId()); // ユーザーIDからメール取得（後述の関数）
			String subject = "[じゅくポン] " + notification.getTitle();
			String body = notification.getMessage();

			emailService.sendEmail(to, subject, body);
		} catch (Exception e) {
			System.err.println("メール送信失敗: " + e.getMessage());
			// ログだけ出して処理継続（必要なら通知テーブルに送信失敗フラグ追加も検討）
		}

		return savedNotification;
	}

	@PutMapping("/{id}/read")
	@Operation(summary = "idのお知らせを既読にする")
	public Notification markAsRead(@PathVariable String id) {
		Notification notification = repository.findById(id).orElse(null);
		if (notification != null) {
			notification.setRead(true);
			return repository.save(notification);
		}
		return null;
	}

	// ← クラスの中にこの private メソッドを追加
	private String getEmailByUserId(String userId) {
		return userRepository.findById(userId)
				.map(user -> user.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: " + userId));
	}
}
