package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "charge_histories")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChargeHistory {
	@Id
	private String id;
	private String studentId;
	private float chargeHours;
	private LocalDateTime CreatedAt;
}
