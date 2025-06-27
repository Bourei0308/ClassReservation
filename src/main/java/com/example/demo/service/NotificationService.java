package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	public void sendNotificationToUser(String userId) {
		// Vue側で subscribe('/api/topic/notice/{userId}') してるため、パスに注意！
		messagingTemplate.convertAndSend("/api/topic/notice/" + userId, "new");
	}
}
