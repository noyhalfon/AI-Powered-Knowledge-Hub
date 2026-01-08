package com.knowledgehub.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a document with its embedding vector for vector search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorDocument {
    private Long documentId;
    private String documentName;
    private String content;
    private DocumentType documentType;
    private List<Double> embedding;
    private String metadata; // JSON string for additional metadata
    
    public VectorDocument(Long documentId, String documentName, String content, 
                         DocumentType documentType, List<Double> embedding) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.content = content;
        this.documentType = documentType;
        this.embedding = embedding;
    }
}

