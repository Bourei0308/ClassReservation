package com.example.demo.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
		List<ClassSchedule> schedules = scheduleRepo.findAll(); // 你可能还想 filter status == 2
		Map<String, String> userIdToName = userRepo.findAll().stream()
				.collect(Collectors.toMap(User::getId, User::getName));

		ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");

		return schedules.stream().map(s -> {
			String id = s.getId();
			String teacherName = userIdToName.getOrDefault(
			    Optional.ofNullable(s.getTeacherId()).orElse(""), "不明"
			);
			String studentName = userIdToName.getOrDefault(
			    Optional.ofNullable(s.getStudentId()).orElse(""), "不明"
			);

			ZonedDateTime start = s.getStartTime().atZone(tokyoZone);
			ZonedDateTime end = s.getEndTime().atZone(tokyoZone);

			String date = start.toLocalDate().toString();
			String time = start.toLocalTime().toString() + "〜" + end.toLocalTime().toString();
			String teacherId = s.getTeacherId();
			String studentId = s.getStudentId();
			String startTime = s.getStartTime().toString(); // UTC ISO文字列
			String endTime = s.getEndTime().toString();

			return new LessonViewDto(
				teacherName, studentName, date, time, s.getStatus(),
				teacherId, studentId, id, "なし", startTime, endTime
			);
		}).toList();
	}

}
