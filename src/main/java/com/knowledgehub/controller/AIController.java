package com.knowledgehub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgehub.models.DTO.ErrorResponse;
import com.knowledgehub.services.AIService;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/ask")
    public ResponseEntity<Object> ask(@RequestBody String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Message cannot be null or empty"));
            }
            
            String answer = aiService.askAboutDocuments(message);
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }
}
