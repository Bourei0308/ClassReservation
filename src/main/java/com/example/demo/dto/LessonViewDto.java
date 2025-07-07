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
	private String startTime; // ISO8601形式的开始时间字符串
    private String endTime;   // ISO8601形式的结束时间字符串
}
