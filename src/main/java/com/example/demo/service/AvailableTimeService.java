package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.AvailableTime;
import com.example.demo.entity.ClassSchedule;
import com.example.demo.repository.AvailableTimeRepository;
import com.example.demo.repository.ClassScheduleRepository;
import com.example.demo.repository.UserRepository;

@Service
public class AvailableTimeService {

	@Autowired
	private AvailableTimeRepository availableTimeRepository;

	@Autowired
	private ClassScheduleRepository classScheduleRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@Transactional
	public void deleteAvailableTime(String availableTimeId) {
		AvailableTime slot = availableTimeRepository.findById(availableTimeId)
				.orElseThrow(() -> new RuntimeException("該当の空き時間が見つかりません"));

		// LocalDateに変換
		LocalDate today = LocalDate.now();
		LocalDate slotDate = slot.getStartTime().toLocalDate();

		List<ClassSchedule> reservations = classScheduleRepository.findByAvailableTimeId(availableTimeId);
		boolean hasApproved = reservations.stream().anyMatch(r -> r.getStatus() == 1);

		if (slotDate.isBefore(today) || hasApproved) {
			return; // 何もしない
		}

		for (ClassSchedule r : reservations) {
			if (r.getStatus() == 0) {
				r.setStatus(2); // 拒否
				classScheduleRepository.save(r);

				// ⛔ NG：r.getUser() は存在しない
				// ✅ OK：studentId を使ってユーザー情報を取得
				userRepository.findById(r.getStudentId()).ifPresent(user -> {
					String email = user.getEmail();
					String name = user.getName();
					emailService.sendRejectionEmail(email, name);
				});
			}
		}

		availableTimeRepository.delete(slot);
	}

}
