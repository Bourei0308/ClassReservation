package com.example.demo.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ClassSchedule;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassScheduleRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.TimeUtils;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // =============================
    // 📧 共通：通知 & メール送信
    // =============================
    @Async
    private void notifyAndSendEmail(String userId, String subject, String body) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(subject);
        notification.setMessage(body);
        notification.setCreatedAt(Instant.now());  // 改成Instant.now()
        notification.setRead(false);
        notificationRepository.save(notification);

        sendEmail(getUser(userId).getEmail(), subject, body);
    }

    // =============================
    // 📧 先生：予約通知
    // =============================
    public void sendTeacherBookingNotification(String scheduleId) {
        ClassSchedule schedule = getSchedule(scheduleId);
        User teacher = getUser(schedule.getTeacherId());
        User student = getUser(schedule.getStudentId());

        String subject = "【じゅくポン】新しい予約があります";
        String body = String.format(
            "%s先生、\n\n%sさんより授業の予約が入りました。\n日時: %s\nご確認の上、承認をお願いいたします。",
            teacher.getName(), student.getName(), formatClassTime(schedule.getStartTime())
        );

        notifyAndSendEmail(teacher.getId(), subject, body);
    }

    // =============================
    // 📧 学生：予約承認通知
    // =============================
    public void sendStudentBookingApproved(String scheduleId) {
        ClassSchedule schedule = getSchedule(scheduleId);
        User teacher = getUser(schedule.getTeacherId());
        User student = getUser(schedule.getStudentId());

        String subject = "【じゅくポン】先生が授業予約を承認しました";
        String body = String.format(
            "%sさん、\n\nご予約の授業が%s先生により承認されました。\n授業日時: %s\n\n準備してお待ちください！",
            student.getName(), teacher.getName(), formatClassTime(schedule.getStartTime())
        );

        notifyAndSendEmail(student.getId(), subject, body);
    }

    // =============================
    // 📧 学生：予約キャンセル通知
    // =============================
    public void sendStudentBookingCancelled(String scheduleId) {
        ClassSchedule schedule = getSchedule(scheduleId);
        User teacher = getUser(schedule.getTeacherId());
        User student = getUser(schedule.getStudentId());

        String subject = "【じゅくポン】先生が授業予約をキャンセルしました";
        String body = String.format(
            "%sさん、\n\nご予約の授業が%s先生によりキャンセルされました。\n授業日時: %s",
            student.getName(), teacher.getName(), formatClassTime(schedule.getStartTime())
        );

        notifyAndSendEmail(student.getId(), subject, body);
    }

    // =============================
    // 📧 先生：生徒が予約を承認前にキャンセル
    // =============================
    public void sendStudentCancelledBeforeApproval(Instant startTime, String teacherId) {
        try {
            User teacher = getUser(teacherId); // 如果你也希望去除这个依赖，可以改为直接传入 teacherEmail

            String title = "【キャンセル】授業予約取消のお知らせ";
            String message = String.format(
                "生徒様が %s の授業予約を承認前にキャンセルしました。", 
                formatClassTime(startTime)
            );

            notifyAndSendEmail(teacher.getId(), title, message);
        } catch (Exception e) {
            System.err.println("❌ キャンセル通知・メール処理中にエラーが発生しました: " + teacherId);
            e.printStackTrace();
            throw new RuntimeException("キャンセル通知・メール送信に失敗しました", e);
        }
    }

    // =============================
    // 📧 パスワード変更完了通知
    // =============================
    public void sendPasswordChanged(String userId) {
        User user = getUser(userId);
        String subject = "【じゅくポン】パスワード変更完了のお知らせ";
        String body = String.format(
            "%sさん\n\nパスワードの変更が正常に完了しました。\n\nご本人による操作でない場合は、速やかにご連絡ください。\n\n引き続き「じゅくポン」をご利用いただけますようお願いいたします。\n\nじゅくポン運営",
            user.getName()
        );

        notifyAndSendEmail(userId, subject, body);
    }

    // =============================
    // 📧 メール送信本体
    // =============================
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ メール送信成功：" + to);
        } catch (Exception e) {
            System.err.println("❌ メール送信失敗：" + e.getMessage());
            throw new RuntimeException("メール送信に失敗しました", e);
        }
    }

    // =============================
    // 🔧 ヘルパー関数
    // =============================
    private String formatClassTime(Instant time) {
        return TimeUtils.formatTimeWithZones(time);
    }

    public ClassSchedule getSchedule(String scheduleId) {
        return classScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("スケジュールが見つかりません"));
    }

    public User getUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
    }
}
