package com.example.demo.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Notification {
    @Id
    private String id;
    private String userId;
    private String title;
    private String message;
    private Instant createdAt;    // 改成Instant
    private boolean isRead;
}
