package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ClassSchedule;
import com.example.demo.repository.ClassScheduleRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

	private final ClassScheduleRepository classScheduleRepository;

	@GetMapping("/completed")
	@Operation(summary = "完了状態の授業一覧取得")
	public List<ClassSchedule> getCompletedLessons() {
		return classScheduleRepository.findByStatus(2); // status = 2 は完了
	}
}
