package com.example.demo.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ClassSchedule;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassScheduleRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;

@Service
public class ReminderService {

	@Autowired
	private ClassScheduleRepository classScheduleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService; // ✅ 统一改为 EmailService

	@Autowired
	private NotificationRepository notificationRepository;

	private static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");

	/**
	 * 授業開始5分前ぴったりの通知（±30秒の範囲）
	 */
	@Scheduled(cron = "0 * * * * *") // 每分钟执行一次
	public void sendRemindersExactly1HourBefore() {
	    ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");
	    ZoneId UTC_ZONE = ZoneOffset.UTC;

	    // 当前日本时间
	    ZonedDateTime nowJST = ZonedDateTime.now(JAPAN_ZONE);

	    // 目标提醒时间是：当前日本时间 + 1小时
	    ZonedDateTime targetStartJST = nowJST.plusHours(1);

	    // 设定误差窗口 ±30秒
	    ZonedDateTime windowStart = targetStartJST.minusSeconds(30);
	    ZonedDateTime windowEnd = targetStartJST.plusSeconds(30);

	    // 由于数据库存的是UTC时间，需要把窗口转换回UTC时间进行查询
	    LocalDateTime windowStartUTC = windowStart.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
	    LocalDateTime windowEndUTC = windowEnd.withZoneSameInstant(UTC_ZONE).toLocalDateTime();

	    // 查询开始时间在这个UTC时间段内，且未提醒过的课程
	    List<ClassSchedule> schedules = classScheduleRepository
	            .findByStartTimeBetweenAndIsRemindedFalse(windowStartUTC, windowEndUTC);

	    for (ClassSchedule schedule : schedules) {
	        Optional<User> userOpt = userRepository.findById(schedule.getStudentId());
	        if (userOpt.isPresent()) {
	            User user = userOpt.get();
	            try {
	                // schedule.getStartTime() 是UTC的LocalDateTime，转成日本时间
	                ZonedDateTime startJST = schedule.getStartTime().atZone(UTC_ZONE).withZoneSameInstant(JAPAN_ZONE);

	                String subject = "【🌸じゅくポンからのリマインド🌸】" + startJST.toLocalDate() + "の授業がもうすぐです！";

	                String body = String.format(
	                    "%sさん、こんにちは！\n授業は %s に開始予定です。\nお忘れないよう、ご準備くださいね📝\n\n…………………………………‥‥・・・*\n学びをもっと楽しく、happyに。\nじゅくポンより🐶\n…………………………………‥‥・・・*",
	                    user.getName(),
	                    startJST.toLocalDate(),
	                    startJST.toLocalTime()
	                );

	                // 发送邮件和通知
	                emailService.sendEmail(user.getEmail(), subject, body);
	                sendNotification(user.getId(), subject, body);

	                // 标记已提醒
	                schedule.setIsReminded(true);
	                classScheduleRepository.save(schedule);

	            } catch (Exception e) {
	                System.err.println("メール送信失敗: " + e.getMessage());
	            }
	        }
	    }
	}

	// 🛎️ 通知保存処理
	private void sendNotification(String userId, String title, String message) {
		Notification notification = new Notification();
		notification.setUserId(userId);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setCreatedAt(Instant.now());
		notification.setRead(false);
		notificationRepository.save(notification);
	}
} 