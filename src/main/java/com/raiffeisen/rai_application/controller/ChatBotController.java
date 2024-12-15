package com.raiffeisen.rai_application.controller;

import com.raiffeisen.rai_application.model.IssueRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class ChatBotController {
    private final ChatClient chatClient;
    private final Map<String, String> trackingData;
    private final List<String> categories;

    @Autowired
    public ChatBotController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        this.trackingData = loadDataFromInternalSystems();
        this.categories = loadCategories();
    }

    private Map<String, String> loadDataFromInternalSystems() {
        Map<String, String> data = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("tracking_data.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("~");
                if (parts.length == 2) {
                    data.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private List<String> loadCategories() {
        List<String> categories = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("categories.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                categories.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @PostMapping("/api/v1/track-issues")
    public ResponseEntity<Map<String, Object>> trackIssue(@RequestBody IssueRequest request) {
        String prompt = "Return one of these categories based on the text: " + String.join(",", categories) + ". Also, extract the location if provided . If no match, return None. " + request.getRequest();
        Map<String, Object> response = new HashMap<>();
        try {
            String incidentCategory = chatClient.prompt(prompt).call().content();

            String location = "";
            if (incidentCategory != null && incidentCategory.contains("Location:")) {
                String[] parts = incidentCategory.split("Location:");
                incidentCategory = parts[0].trim().split("Category:")[1].trim();
                location = parts.length > 1 ? parts[1].trim() : "";
            }

            String key = (incidentCategory != null && (incidentCategory.contains("atm-issue") || incidentCategory.contains("None"))) ? incidentCategory : request.getUserId() + "-" + incidentCategory;
            if ((!location.equals("None") && incidentCategory != null && incidentCategory.contains("atm-issue"))) {
                key += "-" + location;
            }
            String result = trackingData.getOrDefault(key, "At the moment, I cannot answer this question");
            response.put("category", incidentCategory);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
