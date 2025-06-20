package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.Chat;

public interface ChatRepository extends MongoRepository<Chat, String> {
	List<Chat> findByFromUserIdOrToUserId(String fromUserId, String toUserId);
	List<Chat> findByToUserIdAndIsRead(String toUserId, boolean isRead);
}