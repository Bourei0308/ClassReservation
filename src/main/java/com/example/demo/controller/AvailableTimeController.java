package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/available-times")
@Tag(name = "AvailableTime API", description = "空き時間API")
public class AvailableTimeController {
	@Autowired
	private AvailableTimeRepository repository;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@GetMapping
	@Operation(summary = "全ての空き時間取得")
	public List<AvailableTime> getAll() {
		return repository.findAll();
	}

	@GetMapping("/teacher/{teacherId}")
	@Operation(summary = "idで先生の空き時間取得")
	public List<AvailableTime> getByTeacher(@PathVariable("teacherId") String teacherId) {
		return repository.findByTeacherId(teacherId);
	}
	
	@GetMapping("/teacher/{teacherId}/{year}/{month}")
	@Operation(summary = "idで先生の指定した月の空き時間取得")
	public List<AvailableTime> getByTeacherOne(@PathVariable("teacherId") String teacherId,@PathVariable("year") String year,@PathVariable("month") String month) {
		return repository.findByTeacherIdAndYearAndMonth(teacherId, year, month);
	}

	@PostMapping
	@Operation(summary = "空き時間追加")
	public AvailableTime create(@RequestBody AvailableTime time) {
	    messagingTemplate.convertAndSend("/api/topic/calendar/","data");
		return repository.save(time);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "idで空き時間削除")
	public void delete(@PathVariable("id") String id) {
	    messagingTemplate.convertAndSend("/api/topic/calendar/","data");
		repository.deleteById(id);
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "idで空き時間を更新")
	public AvailableTime update(@PathVariable("id") String id, @RequestBody AvailableTime newTime) {
	    messagingTemplate.convertAndSend("/api/topic/calendar/","data");
	    return repository.findById(id).map(existingTime -> {
	        existingTime.setStartTime(newTime.getStartTime());
	        existingTime.setEndTime(newTime.getEndTime());
	        return repository.save(existingTime);
	    }).orElse(null);
	}
}
