package com.example.demo.controller;

import java.time.Instant;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification API", description = "お知らせAPI")
public class NotificationController {
	@Autowired
	private NotificationRepository repository;

	@GetMapping("/user/{userId}")
	@Operation(summary = "全てのお知らせ取得")
	public List<Notification> getByUser(@PathVariable("userId") String userId) {
		return repository.findByUserId(userId);
	}

	@PostMapping
	@Operation(summary = "お知らせの追加とメール送信")
	public Notification create(@RequestBody Notification notification) {
		notification.setCreatedAt(Instant.now());
		notification.setRead(false);
		Notification savedNotification = repository.save(notification);
		return savedNotification;
	}

	@PutMapping("/{id}/read")
	@Operation(summary = "idのお知らせを既読にする")
	public Notification markAsRead(@PathVariable("id") String id) {
		Notification notification = repository.findById(id).orElse(null);
		if (notification != null) {
			notification.setRead(true);
			return repository.save(notification);
		}
		return null;
	}
}
