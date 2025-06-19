package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "available_times")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AvailableTime {
	@Id
	private String id;
	private String teacherId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}
