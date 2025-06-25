package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ClassSchedule;
import com.example.demo.repository.ClassScheduleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/class-schedules")
@Tag(name = "ClassSchedule API", description = "授業API")
public class ClassScheduleController {
	@Autowired
	private ClassScheduleRepository repository;

	@GetMapping
	@Operation(summary = "全ての授業取得")
	public List<ClassSchedule> getAll() {
		return repository.findAll();
	}

	@GetMapping("/student/{studentId}")
	@Operation(summary = "idで生徒の授業を取得")
	public List<ClassSchedule> getByStudent(@PathVariable String studentId) {
		return repository.findByStudentId(studentId);
	}

	@GetMapping("/teacher/{teacherId}")
	@Operation(summary = "idで先生の授業を取得")
	public List<ClassSchedule> getByTeacher(@PathVariable String teacherId) {
		return repository.findByTeacherId(teacherId);
	}

	@PostMapping
	@Operation(summary = "授業追加")
	public ClassSchedule create(@RequestBody ClassSchedule schedule) {
		return repository.save(schedule);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "idで授業を削除")
	public void delete(@PathVariable String id) {
		repository.deleteById(id);
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "idで授業を更新")
	public ClassSchedule update(@PathVariable String id, @RequestBody ClassSchedule updatedSchedule) {
	    return repository.findById(id)
	        .map(schedule -> {
	            schedule.setStartTime(updatedSchedule.getStartTime());
	            schedule.setEndTime(updatedSchedule.getEndTime());
	            schedule.setCreatedAt(updatedSchedule.getCreatedAt());
	            schedule.setStatus(updatedSchedule.getStatus());
	            // 添加你需要更新的字段
	            return repository.save(schedule);
	        })
	        .orElseThrow(() -> new RuntimeException("指定された授業が見つかりません: " + id));
	}
}
