package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LessonViewDto;
import com.example.demo.entity.ClassSchedule;
import com.example.demo.entity.User;
import com.example.demo.repository.ClassScheduleRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

	private final ClassScheduleRepository scheduleRepo;
	private final UserRepository userRepo;

	@GetMapping("/completed")
	public List<LessonViewDto> getCompletedLessons() {
		List<ClassSchedule> schedules = scheduleRepo.findAll(); // status 2 = 完了
		Map<String, String> userIdToName = userRepo.findAll().stream()
				.collect(Collectors.toMap(User::getId, User::getName));

		return schedules.stream().map(s -> {
			String id = s.getId();
			String teacherName = userIdToName.getOrDefault(
				    Optional.ofNullable(s.getTeacherId()).orElse(""), "不明"
				);
				String studentName = userIdToName.getOrDefault(
				    Optional.ofNullable(s.getStudentId()).orElse(""), "不明"
				);
			String date = s.getStartTime().toLocalDate().toString();
			String time = s.getStartTime().toLocalTime().toString() + "〜" + s.getEndTime().toLocalTime().toString();
			String teacherId=s.getTeacherId();
			String studentId=s.getStudentId();
			return new LessonViewDto(
					teacherName, studentName, date, time, s.getStatus(),teacherId,studentId,id, "なし");
		}).toList();
	}
}
