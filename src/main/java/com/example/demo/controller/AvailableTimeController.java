package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.AvailableTime;
import com.example.demo.repository.AvailableTimeRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/available-times")
@Tag(name = "AvailableTime API", description = "空き時間API")
public class AvailableTimeController {
	@Autowired
	private AvailableTimeRepository repository;

	@GetMapping
	@Operation(summary = "全ての空き時間取得")
	public List<AvailableTime> getAll() {
		return repository.findAll();
	}

	@GetMapping("/teacher/{teacherId}")
	@Operation(summary = "idで先生の空き時間取得")
	public List<AvailableTime> getByTeacher(@PathVariable String teacherId) {
		return repository.findByTeacherId(teacherId);
	}

	@PostMapping
	@Operation(summary = "空き時間追加")
	public AvailableTime create(@RequestBody AvailableTime time) {
		return repository.save(time);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "idで空き時間削除")
	public void delete(@PathVariable String id) {
		repository.deleteById(id);
	}
}
