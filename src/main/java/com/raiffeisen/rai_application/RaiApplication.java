package com.raiffeisen.rai_application;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RaiApplication {

//	@Bean
//	public CommandLineRunner runner(ChatClient.Builder builder) {
//		return args -> {
//			ChatClient chatClient = builder.build();
//			String response = chatClient.prompt("Cka eshte Spring Boot").call().content();
//			System.out.println(response);
//		};
//	}

	public static void main(String[] args) {
		SpringApplication.run(RaiApplication.class, args);
	}

}
