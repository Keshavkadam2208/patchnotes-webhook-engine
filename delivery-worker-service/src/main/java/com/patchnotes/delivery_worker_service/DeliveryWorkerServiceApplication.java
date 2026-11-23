package com.patchnotes.delivery_worker_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeliveryWorkerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryWorkerServiceApplication.class, args);
        System.out.println("hey im in delivery_worker_service");
	}

}
