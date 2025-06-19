package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "charge_histories")
@Data
public class ChargeHistory {
	@Id
	private String id;
	private String studentId;
	private int chargeHours;
}
