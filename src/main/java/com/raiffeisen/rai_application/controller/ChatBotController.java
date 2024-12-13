package com.raiffeisen.rai_application.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatBotController {

    private final ChatClient chatClient;
    private final Map<String, String> trackingData;

    @Autowired
    public ChatBotController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        this.trackingData = loadTrackingData();
    }

    private Map<String, String> loadTrackingData() {
        Map<String, String> data = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("tracking_data.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    data.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @GetMapping("/api/v1/track")
    public String trackIssue(@RequestParam String userId, @RequestParam String freeText) {
        String prompt = "Return one of these categories based on the text ,mobile-sms,cardDelay,cardLost,cardDamaged and if you don't match the request on any of these return None "+freeText;
        try {
            // Use ChatClient to determine the incident category from free text
            String incidentCategory = chatClient.prompt(prompt).call().content();

            // Build the key for tracking data lookup
            String key = userId + "-" + incidentCategory;
            // Optionally send the response back to the chatbot client
            return trackingData.getOrDefault(key, "Per momentin nuk mund te pergjigjem per kete pytje.");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/api/v1/chat")
    public String chat(@RequestParam String prompt) {
        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
