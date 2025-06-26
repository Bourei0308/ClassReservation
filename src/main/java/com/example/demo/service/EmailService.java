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
	
	// =============================
    // 📧 先生：予約通知
    // =============================
	public void sendTeacherBookingNotification(String scheduleId) {
		ClassSchedule schedule = getSchedule(scheduleId);
    	User teacher = getUser(schedule.getTeacherId());
    	User student = getUser(schedule.getStudentId());
    	
		String teacherName = teacher.getName();
		String studentName = student.getName();
		String classTime = formatClassTime(schedule.getStartTime());
		
		String subject = "【じゅくポン】新しい予約があります";
		String body = String.format(
				"%s先生、\n\n%sさんより授業の予約が入りました。\n日時: %s\nご確認の上、承認をお願いいたします。",
				teacherName, studentName, classTime);
		sendEmail(teacher.getEmail(), subject, body);
	}
	
	// =============================
    // 📧 学生：先生が予約承認
    // =============================
    public void sendStudentBookingApproved(String scheduleId) {
    	ClassSchedule schedule = getSchedule(scheduleId);
    	User teacher = getUser(schedule.getTeacherId());
    	User student = getUser(schedule.getStudentId());

        String studentName = student.getName();
        String teacherName = teacher.getName();
        String classTime = formatClassTime(schedule.getStartTime());

        String subject = "【じゅくポン】先生が授業予約を承認しました";
        String body = String.format(
            "%sさん、\n\nご予約の授業が%s先生により承認されました。\n授業日時: %s\n\n準備してお待ちください！",
            studentName,teacherName, classTime
        );
        sendEmail(student.getEmail(), subject, body);
    }
    
 // =============================
    // 📧 学生：先生が予約キャンセル
    // =============================
    public void sendStudentBookingCancelled(String scheduleId) {
    	ClassSchedule schedule = getSchedule(scheduleId);
    	User teacher = getUser(schedule.getTeacherId());
    	User student = getUser(schedule.getStudentId());

        String studentName = student.getName();
        String teacherName = teacher.getName();
        String classTime = formatClassTime(schedule.getStartTime());

        String subject = "【じゅくポン】先生が授業予約をキャンセルしました";
        String body = String.format(
            "%sさん、\n\nご予約の授業が%s先生によりキャンセルされました。\n授業日時: %s",
            studentName,teacherName, classTime
        );
        sendEmail(student.getEmail(), subject, body);
    }
    
 // =============================
    // 📧 パスワード変更完了通知
    // =============================
    public void sendPasswordChanged(String userId) {
        User user = getUser(userId);

        String userName = user.getName();

        String subject = "【じゅくポン】パスワード変更完了のお知らせ";
        String body = String.format(
            "%sさん\n\nパスワードの変更が正常に完了しました。\n\nご本人による操作でない場合は、速やかにご連絡ください。\n\n引き続き「じゅくポン」をご利用いただけますようお願いいたします。\n\nじゅくポン運営",
            userName
        );
        sendEmail(user.getEmail(), subject, body);
    }

	// 日時フォーマット関数
	private String formatClassTime(LocalDateTime time) {
		return time.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
	}
	
	private ClassSchedule getSchedule(String scheduleId) {
        return classScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("スケジュールが見つかりません"));
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
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