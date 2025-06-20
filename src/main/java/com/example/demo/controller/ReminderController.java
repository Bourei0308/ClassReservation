package com.example.demo.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ReminderService;

@RestController
@RequestMapping("/api/reminder")
public class ReminderController {

	@Autowired
	private ReminderService reminderService;

	@PostMapping("/send-custom")
	public String sendRemindersForDate(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		reminderService.sendRemindersFor(date);
		return "リマインド送信完了：" + date;
	}

	@PostMapping("/send-5min-before")
	public String sendRemindersFor5MinBefore() {
		reminderService.sendRemindersExactly5MinBefore();
		return "5分前リマインド送信を実行しました";
	}
}
