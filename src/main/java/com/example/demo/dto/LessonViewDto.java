package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LessonViewDto {
	private String teacherName;
	private String studentName;
	private String date;
	private String time;
	private int status;
	private String teacherId;
	private String studentId;
	private String id;
	private String comment;
}
