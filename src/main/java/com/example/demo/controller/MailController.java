package com.example.demo.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MailRequest;
import com.example.demo.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/mail")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "メール送信API", description = "SMTPによるメール送信API")
public class MailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "SMTPでメール送信（メールアドレスと本文を直接指定）")
    public ResponseEntity<Map<String, String>> sendMail(@RequestBody MailRequest mailRequest) {
        try {
            emailService.sendEmail(
                    mailRequest.getTo(),
                    mailRequest.getSubject(),
                    mailRequest.getBody()
            );
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "メール送信に成功しました。"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "送信エラー: " + e.getMessage()));
        }
    }

    @PostMapping("/notify/teacher")
    @Operation(summary = "先生への予約通知メール送信")
    public ResponseEntity<String> notifyTeacher(@RequestBody Map<String, String> payload) {
        try {
            String scheduleId = payload.get("classScheduleId");
            emailService.sendTeacherBookingNotification(scheduleId);
            return ResponseEntity.ok("先生に通知メールを送信しました。");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("通知メール送信失敗: " + e.getMessage());
        }
    }

    @PostMapping("/notify/student/confirmed")
    @Operation(summary = "学生：先生が予約承認")
    public ResponseEntity<String> notifyStudentConfirm(@RequestBody Map<String, String> payload) {
        try {
            String scheduleId = payload.get("classScheduleId");
            emailService.sendStudentBookingApproved(scheduleId);
            return ResponseEntity.ok("学生に通知メールを送信しました。");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("通知メール送信失敗: " + e.getMessage());
        }
    }

    @PostMapping("/notify/student/cancelled")
    @Operation(summary = "学生：先生が予約キャンセル")
    public ResponseEntity<String> notifyStudentCancelled(@RequestBody Map<String, String> payload) {
        try {
            String scheduleId = payload.get("classScheduleId");
            emailService.sendStudentBookingCancelled(scheduleId);
            return ResponseEntity.ok("学生に通知メールを送信しました。");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("通知メール送信失敗: " + e.getMessage());
        }
    }

    @PostMapping("/notify/student/cancelled/beforeapproval")
    @Operation(summary = "先生：学生が承認前に予約キャンセル")
    public ResponseEntity<String> notifyStudentCancelledBeforeApproval(@RequestBody Map<String, String> payload) {
        try {
            String teacherId = payload.get("teacherId");
            String startTimeStr = payload.get("startTime");

            // 直接用Instant解析，前端传来的时间格式必须是ISO 8601格式，比如 "2025-07-08T03:00:00Z"
            Instant startTime = Instant.parse(startTimeStr);

            emailService.sendStudentCancelledBeforeApproval(startTime, teacherId);

            return ResponseEntity.ok("通知とメールを送信しました");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("通知送信に失敗しました: " + e.getMessage());
        }
    }


    @PostMapping("/notify/passwordchange")
    @Operation(summary = "パスワード変更完了通知")
    public ResponseEntity<String> notifyPasswordChanged(@RequestBody Map<String, String> payload) {
        try {
            String userId = payload.get("userId");
            emailService.sendPasswordChanged(userId);
            return ResponseEntity.ok("学生に通知メールを送信しました。");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("通知メール送信失敗: " + e.getMessage());
        }
    }
    
    
}
