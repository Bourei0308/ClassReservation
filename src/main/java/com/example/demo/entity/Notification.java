package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "notifications")
@Data
public class Notification {
	@Id
	private String id;
	private String userId;
	private String title;
	private String message;
	private LocalDateTime createdAt;
	private boolean isRead;
}
