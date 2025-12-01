package com.kubecon.processpayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams

public class ProcesspaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcesspaymentsApplication.class, args);
	}

}
