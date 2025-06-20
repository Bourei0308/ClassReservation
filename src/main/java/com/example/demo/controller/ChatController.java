package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

	@GetMapping("/user/{userId}")
	@Operation(summary = "全てのチャット履歴取得")
	public List<Chat> getUserChats(@PathVariable String userId) {
		return repository.findByFromUserIdOrToUserId(userId, userId);
	}

	@PostMapping
	@Operation(summary = "チャット追加")
	public Chat sendMessage(@RequestBody Chat chat) {
		return repository.save(chat);
	}
	
	@GetMapping("/unread/{userId}")
	@Operation(summary = "未読メッセージ取得")
	public List<Chat> getUnreadMessages(@PathVariable String userId) {
	    return repository.findByToUserIdAndIsRead(userId, false);
	}
}
