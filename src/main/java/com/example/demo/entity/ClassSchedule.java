package com.example.demo.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "class_schedules")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClassSchedule {

	@Id
	private String id;
	private String studentId;
	private String teacherId;
	private Instant createdAt;
	private Instant startTime;
	private Instant endTime;
	private int status; // 0 = pending, 1 = confirmed, 2 = completed, etc.
	private Boolean isReminded = false;
}
