package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.AvailableTime;
import com.example.demo.repository.AvailableTimeRepository;
import com.example.demo.service.AvailableTimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/available-times")
@Tag(name = "AvailableTime API", description = "空き時間API")
public class AvailableTimeController {

	@Autowired
	private AvailableTimeRepository repository;

	@Autowired
	private AvailableTimeService availableTimeService;

	@GetMapping
	@Operation(summary = "全ての空き時間取得")
	public List<AvailableTime> getAll() {
		return repository.findAll();
	}

	@GetMapping("/teacher/{teacherId}")
	@Operation(summary = "先生IDで空き時間を取得")
	public List<AvailableTime> getByTeacher(@PathVariable String teacherId) {
		return repository.findByTeacherId(teacherId);
	}

	@GetMapping("/teacher/{teacherId}/{year}/{month}")
	@Operation(summary = "先生IDと年月で空き時間取得")
	public List<AvailableTime> getByTeacherByMonth(
			@PathVariable String teacherId,
			@PathVariable int year,
			@PathVariable int month) {

		LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
		LocalDateTime end = start.plusMonths(1).minusSeconds(1);

		return repository.findByTeacherIdAndStartTimeBetween(teacherId, start, end);
	}

	@PostMapping
	@Operation(summary = "空き時間追加")
	public AvailableTime create(@RequestBody AvailableTime time) {
		return repository.save(time);
	}

	@PutMapping("/{id}")
	@Operation(summary = "IDで空き時間を更新")
	public AvailableTime update(@PathVariable String id, @RequestBody AvailableTime newTime) {
		return repository.findById(id).map(existingTime -> {
			existingTime.setStartTime(newTime.getStartTime());
			existingTime.setEndTime(newTime.getEndTime());
			return repository.save(existingTime);
		}).orElse(null);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "IDで空き時間削除（ロジック付き）")
	public ResponseEntity<?> deleteAvailableTime(@PathVariable String id) {
		availableTimeService.deleteAvailableTime(id); // 論理付き削除（予約状態チェックなど）
		return ResponseEntity.ok("空き時間削除完了");
	}
}
