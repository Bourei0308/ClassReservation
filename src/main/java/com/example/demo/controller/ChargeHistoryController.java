package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ChargeHistory;
import com.example.demo.repository.ChargeHistoryRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/charges")
@Tag(name = "ChargeHistory API", description = "チャージAPI")
@CrossOrigin(origins = "http://localhost:5173") // Vueポートを明示
public class ChargeHistoryController {
	@Autowired
	private ChargeHistoryRepository chargeHistoryRepository;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@GetMapping
	@Operation(summary = "全てのチャージ履歴取得（管理者用）")
	public List<ChargeHistory> getAllCharges() {
		return chargeHistoryRepository.findAll();
	}

	@GetMapping("/users/{userId}")
	@Operation(summary = "特定のユーザーのチャージ履歴取得")
	public List<ChargeHistory> getChargesByUser(@PathVariable("userId") String userId) {
		return chargeHistoryRepository.findByStudentId(userId);
	}

		@PostMapping("/users/{userId}")
		@Operation(summary = "チャージ履歴の追加")
		public ChargeHistory addCharge(@PathVariable("userId") String userId, @RequestBody ChargeHistory charge) {
			charge.setStudentId(userId);
			messagingTemplate.convertAndSend("/api/topic/calendar/","data");
			return chargeHistoryRepository.save(charge);
		}

	@PutMapping("/{id}")
	@Operation(summary = "IDでチャージ時間を更新")
	public ChargeHistory updateChargeById(@PathVariable("id") String id, @RequestParam float deltaHours) {
		ChargeHistory history = chargeHistoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("指定されたIDのチャージ履歴が見つかりません"));

		float updatedHours = history.getChargeHours() + deltaHours;

		if (updatedHours < 0) {
			throw new RuntimeException("残り時間が0未満になるため、処理を中止しました");
		}
		history.setChargeHours(updatedHours);
		return chargeHistoryRepository.save(history);
	}

	@GetMapping("/users/{userId}/total")
	@Operation(summary = "特定ユーザーのチャージ合計時間を取得")
	public double getTotalChargeByUser(@PathVariable("userId") String userId) {
		List<ChargeHistory> historyList = chargeHistoryRepository.findByStudentId(userId);
		return historyList.stream()
				.mapToDouble(ChargeHistory::getChargeHours)
				.sum();
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "チャージ履歴をIDで削除")
	public void deleteChargeById(@PathVariable("id") String id) {
		
	    chargeHistoryRepository.deleteById(id);
	    messagingTemplate.convertAndSend("/api/topic/calendar/","data");
	}

}
