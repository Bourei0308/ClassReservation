package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.auth.AccessTokenProvider;
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
	private GmailService gmailService;

	@Autowired
	private AccessTokenProvider accessTokenProvider;

	@Autowired
	private NotificationRepository notificationRepository;

	private static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");

	/**
	 * 授業開始5分前ぴったりの通知（±30秒の範囲）
	 * → 毎分実行される前提で設計
	 */
	//	@Scheduled(cron = "0 * * * * *") // 毎分実行
	//	public void sendRemindersExactly5MinBefore() {
	//		ZonedDateTime nowJST = ZonedDateTime.now(JAPAN_ZONE);
	//		LocalDateTime targetStartTime = nowJST.plusMinutes(5).toLocalDateTime();
	//
	//		LocalDateTime windowStart = targetStartTime.minusSeconds(30);
	//		LocalDateTime windowEnd = targetStartTime.plusSeconds(30);
	//
	//		// ⭐ isReminded=false のみに絞る
	//		List<ClassSchedule> schedules = classScheduleRepository
	//				.findByStartTimeBetweenAndIsRemindedFalse(windowStart, windowEnd);
	//
	//		for (ClassSchedule schedule : schedules) {
	//			Optional<User> userOpt = userRepository.findById(schedule.getStudentId());
	//			if (userOpt.isPresent()) {
	//				User user = userOpt.get();
	//				try {
	//					String accessToken = accessTokenProvider.getAccessToken();
	//					gmailService.sendEmail(
	//							accessToken,
	//							user.getEmail(),
	//							"【リマインド】まもなく授業が始まります（5分前）",
	//							String.format("%sさん、%s に授業が開始されます。ご準備ください！",
	//									user.getName(),
	//									schedule.getStartTime().atZone(JAPAN_ZONE).toLocalTime()));
	//
	//					// ⭐ メール送信後に isReminded = true
	//					schedule.setIsReminded(true);
	//					classScheduleRepository.save(schedule);
	//
	//				} catch (Exception e) {
	//					System.err.println("メール送信失敗: " + e.getMessage());
	//				}
	//			}
	//		}
	//	}

	private void sendNotification(String userId, String title, String message) {
		Notification notification = new Notification();
		notification.setUserId(userId);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setCreatedAt(LocalDateTime.now());
		notification.setRead(false);
		notificationRepository.save(notification);
	}

	@Scheduled(cron = "0 * * * * *") // 毎分実行
	public void sendRemindersExactly5MinBefore() {
		ZonedDateTime nowJST = ZonedDateTime.now(JAPAN_ZONE);
		LocalDateTime targetStartTime = nowJST.plusMinutes(5).toLocalDateTime();

		LocalDateTime windowStart = targetStartTime.minusSeconds(30);
		LocalDateTime windowEnd = targetStartTime.plusSeconds(30);

		List<ClassSchedule> schedules = classScheduleRepository
				.findByStartTimeBetweenAndIsRemindedFalse(windowStart, windowEnd);

		for (ClassSchedule schedule : schedules) {
			Optional<User> userOpt = userRepository.findById(schedule.getStudentId());
			if (userOpt.isPresent()) {
				User user = userOpt.get();
				try {
					String accessToken = accessTokenProvider.getAccessToken();

					String subject = "【🌸じゅくポンからのリマインド🌸】%sの授業がもうすぐです！"
							.formatted(schedule.getStartTime().toLocalDate());
					String body = String.format(
							"%sさん、こんにちは！\n\n明日（%s）は授業の予定日です✨\n授業は %s に開始予定です。\nお忘れないよう、ご準備くださいね📝\n\nそれでは、明日の授業でお会いしましょう！\n\n…………………………………‥‥・・・*\n学びをもっと楽しく、happyに。\nじゅくポンより🐶\n…………………………………‥‥・・・*",
							user.getName(),
							schedule.getStartTime().toLocalDate(),
							schedule.getStartTime().atZone(JAPAN_ZONE).toLocalTime());

					// 📧 メール送信
					gmailService.sendEmail(accessToken, user.getEmail(), subject, body);

					// 🛎️ 通知も送信
					sendNotification(user.getId(), subject, body);

					// ✅ メール送信成功 → isReminded = true
					schedule.setIsReminded(true);
					classScheduleRepository.save(schedule);

				} catch (Exception e) {
					System.err.println("メール送信失敗: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Swaggerなどから任意の日付を指定して実行
	 */
	public void sendRemindersFor(LocalDate date) {
		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = start.plusDays(1);

		List<ClassSchedule> schedules = classScheduleRepository.findByStartTimeBetween(start, end);

		for (ClassSchedule schedule : schedules) {
			Optional<User> userOpt = userRepository.findById(schedule.getStudentId());
			if (userOpt.isPresent()) {
				User user = userOpt.get();
				try {
					String accessToken = accessTokenProvider.getAccessToken();

					String subject = "【🌸じゅくポンからのリマインド🌸】" + date + "の授業がもうすぐです！";
					String body = user.getName() + "さん、こんにちは！\n\n"
							+ "明日（" + date + "）は授業の予定日です✨\n"
							+ "授業は " + schedule.getStartTime().atZone(JAPAN_ZONE).toLocalTime() + " に開始予定です。\n"
							+ "お忘れないよう、ご準備くださいね📝\n\n"
							+ "それでは、明日の授業でお会いしましょう！\n\n"
							+ "…………………………………‥‥・・・*\n"
							+ "  学びをもっと楽しく、happyに。\n"
							+ "　じゅくポンより🐶\n"
							+ "…………………………………‥‥・・・*";

					// 📧 メール送信
					gmailService.sendEmail(accessToken, user.getEmail(), subject, body);

					// 🛎️ 通知送信
					sendNotification(user.getId(), subject, body);

				} catch (Exception e) {
					System.err.println("メール送信失敗: " + e.getMessage());
				}
			}
		}
	}

}
