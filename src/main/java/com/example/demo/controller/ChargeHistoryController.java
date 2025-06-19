package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
public class ChargeHistoryController {
	@Autowired
	private ChargeHistoryRepository chargeHistoryRepository;

	@GetMapping
	@Operation(summary = "全てのチャージ履歴取得（管理者用）")
	public List<ChargeHistory> getAllCharges() {
		return chargeHistoryRepository.findAll();
	}

	@GetMapping("/users/{userId}")
	@Operation(summary = "特定のユーザーのチャージ履歴取得")
	public List<ChargeHistory> getChargesByUser(@PathVariable String userId) {
		return chargeHistoryRepository.findByStudentId(userId);
	}

	@PostMapping("/users/{userId}")
	@Operation(summary = "チャージ履歴の追加")
	public ChargeHistory addCharge(@PathVariable String userId, @RequestBody ChargeHistory charge) {
		charge.setStudentId(userId);
		return chargeHistoryRepository.save(charge);
	}

	@PutMapping("/{id}")
	@Operation(summary = "IDでチャージ時間を更新")
	public ChargeHistory updateChargeById(@PathVariable String id, @RequestParam int deltaHours) {
		ChargeHistory history = chargeHistoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("指定されたIDのチャージ履歴が見つかりません"));

		int updatedHours = history.getChargeHours() + deltaHours;

		if (updatedHours < 0) {
			throw new RuntimeException("残り時間が0未満になるため、処理を中止しました");
		}
		history.setChargeHours(updatedHours);
		return chargeHistoryRepository.save(history);
	}
}
