package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> {
	List<Notification> findByUserId(String userId);
}
