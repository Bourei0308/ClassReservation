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
	private String comment;
}
