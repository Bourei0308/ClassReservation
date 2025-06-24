package com.example.demo.service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.auth.AccessTokenProvider;
import com.example.demo.entity.ClassSchedule;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassScheduleRepository;
import com.example.demo.repository.UserRepository;
@Service
public class EmailService {
	@Autowired
	private GmailService gmailService;
	@Autowired
	private AccessTokenProvider accessTokenProvider;
	@Autowired
	private ClassScheduleRepository classScheduleRepository;
	@Autowired
	private UserRepository userRepository;
	public void sendTeacherBookingNotification(String scheduleId) {
		ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new RuntimeException("スケジュールが見つかりません"));
		User teacher = userRepository.findById(schedule.getTeacherId())
				.orElseThrow(() -> new RuntimeException("先生が見つかりません"));
		User student = userRepository.findById(schedule.getStudentId())
				.orElseThrow(() -> new RuntimeException("生徒が見つかりません"));
		String teacherName = teacher.getName();
		String studentName = student.getName();
		String classTime = formatClassTime(schedule.getStartTime());
		String subject = "【じゅくポン】新しい予約があります";
		String body = String.format(
				"%s先生、\n\n%sさんより授業の予約が入りました。\n日時: %s\nご確認の上、承認をお願いいたします。",
				teacherName, studentName, classTime);
		sendEmail(teacher.getEmail(), subject, body);
	}
	// 日時フォーマット関数
	private String formatClassTime(LocalDateTime time) {
		return time.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
	}
	/**
	 * 汎用メール送信メソッド
	 */
	public void sendEmail(String to, String subject, String body) {
		try {
			String accessToken = accessTokenProvider.getAccessToken();
			gmailService.sendEmail(accessToken, to, subject, body);
			System.out.println(":チェックマーク_緑: メール送信成功：" + to);
		} catch (Exception e) {
			System.err.println(":x: メール送信失敗：" + e.getMessage());
			throw new RuntimeException("メール送信に失敗しました", e);
		}
	}
}