package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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

	//	@PostMapping("/users/{userId}")
	//	@Operation(summary = "チャージ履歴の追加")
	//	public ChargeHistory addCharge(@PathVariable String userId, @RequestBody ChargeHistory charge) {
	//		charge.setStudentId(userId);
	//		return chargeHistoryRepository.save(charge);
	//	}

	@PostMapping("/users/{userId}")
	@Operation(summary = "チャージ時間を加算する")
	public ChargeHistory addOrUpdateCharge(@PathVariable String userId, @RequestBody ChargeHistory charge) {
		// 学生の過去のチャージ履歴をすべて取得
		List<ChargeHistory> historyList = chargeHistoryRepository.findByStudentId(userId);

		ChargeHistory target;

		if (historyList.isEmpty()) {
			// 履歴が存在しない場合は新規作成
			target = new ChargeHistory();
			target.setStudentId(userId);
			target.setChargeHours(charge.getChargeHours());
		} else {
			// 履歴がある場合は最新のものに加算（最初の1件だけ加算する例）
			target = historyList.get(0);
			target.setChargeHours(target.getChargeHours() + charge.getChargeHours());
		}

		return chargeHistoryRepository.save(target);
	}

	@PutMapping("/{id}")
	@Operation(summary = "IDでチャージ時間を更新")
	public ChargeHistory updateChargeById(@PathVariable String id, @RequestParam float deltaHours) {
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
	public double getTotalChargeByUser(@PathVariable String userId) {
		List<ChargeHistory> historyList = chargeHistoryRepository.findByStudentId(userId);
		return historyList.stream()
				.mapToDouble(ChargeHistory::getChargeHours)
				.sum();
	}

}
