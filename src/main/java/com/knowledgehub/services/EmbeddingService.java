package com.knowledgehub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating embeddings from text using OpenAI's embedding API via HTTP
 */
@Service
public class EmbeddingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    private static final String EMBEDDING_API_URL = "https://api.openai.com/v1/embeddings";
    private static final String EMBEDDING_MODEL = "text-embedding-3-small";

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate embedding vector for a single text using OpenAI's embedding API
     * @param text The text to embed
     * @return List of doubles representing the embedding vector
     */
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        try {
            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", EMBEDDING_MODEL);
            requestBody.put("input", text);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Call OpenAI API
            ResponseEntity<String> response = restTemplate.exchange(
                EMBEDDING_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            JsonNode dataArray = jsonResponse.get("data");
            
            if (dataArray == null || !dataArray.isArray() || dataArray.size() == 0) {
                throw new RuntimeException("Failed to generate embedding: invalid response");
            }
            
            JsonNode embeddingArray = dataArray.get(0).get("embedding");
            List<Double> embedding = new ArrayList<>();
            
            for (JsonNode value : embeddingArray) {
                embedding.add(value.asDouble());
            }
            
            return embedding;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings for multiple texts
     * @param texts List of texts to embed
     * @return List of embedding vectors (one per text)
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Texts cannot be null or empty");
        }
        
        // For simplicity, generate embeddings one by one
        // In production, you could batch them
        return texts.stream()
            .map(this::generateEmbedding)
            .collect(Collectors.toList());
    }

    /**
     * Generate embedding for a query (used for similarity search)
     * @param query The search query
     * @return List of doubles representing the query embedding
     */
    public List<Double> generateQueryEmbedding(String query) {
        return generateEmbedding(query);
    }
}

