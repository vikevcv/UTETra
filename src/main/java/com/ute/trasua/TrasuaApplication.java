package com.ute.trasua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
// ponytail: default async executor (1 thread/mail), custom pool only if mail volume matters
@EnableAsync
public class TrasuaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrasuaApplication.class, args);
	}

}
