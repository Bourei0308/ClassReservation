package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "chats")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Chat {
	@Id
	private String id;
	private String fromUserId;
	private String toUserId;
	private String message;
	private LocalDateTime createdAt;
	@JsonProperty("isRead")
	private boolean isRead;
}
