package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Chat;
import com.example.demo.repository.ChatRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chat API", description = "チャットAPI")
public class ChatController {
	@Autowired
	private ChatRepository repository;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@GetMapping("/user/{userId}")
	@Operation(summary = "全てのチャット履歴取得")
	public List<Chat> getUserChats(@PathVariable String userId) {
		return repository.findByFromUserIdOrToUserId(userId, userId);
	}

	@PostMapping
	@Operation(summary = "チャット追加")
	public Chat sendMessage(@RequestBody Chat chat) {
	    // 设置时间和未读状态（可选）
	    chat.setCreatedAt(LocalDateTime.now());
	    chat.setRead(false);

	    // ⚠️ 先保存，MongoDB会生成id
	    Chat savedChat = repository.save(chat);

	    // 再发送给对方
	    messagingTemplate.convertAndSend("/api/topic/chats/" + savedChat.getToUserId(), savedChat);

	    // 返回给发送者
	    return savedChat;
	}
	
	@GetMapping("/unread/{userId}")
	@Operation(summary = "未読メッセージ取得")
	public List<Chat> getUnreadMessages(@PathVariable String userId) {
	    return repository.findByToUserIdAndIsRead(userId, false);
	}
	
	@PostMapping("/mark-read/{id}")
	@Operation(summary = "指定IDのチャットを既読にする")
	public void markAsReadById(@PathVariable String id) {
	    Chat chat = repository.findById(id).orElse(null);
	    if (chat != null && !chat.isRead()) {
	        chat.setRead(true);
	        Chat updatedChat =repository.save(chat);
	        messagingTemplate.convertAndSend("/api/topic/read-status/" + chat.getFromUserId(), updatedChat);
	    }
	}
}
