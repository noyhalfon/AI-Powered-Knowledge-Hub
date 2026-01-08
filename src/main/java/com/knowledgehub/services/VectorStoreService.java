package com.knowledgehub.services;

import com.knowledgehub.models.Document;
import com.knowledgehub.models.DocumentType;
import com.knowledgehub.models.VectorDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory vector store for semantic search
 * Uses cosine similarity for finding similar documents
 */
@Service
public class VectorStoreService {

    private final EmbeddingService embeddingService;
    
    // In-memory storage: documentId -> VectorDocument
    private final Map<Long, VectorDocument> vectorStore = new ConcurrentHashMap<>();
    
    @Autowired
    public VectorStoreService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * Add a document to the vector store with its embedding
     * @param document The document to add
     */
    public void addDocument(Document document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("Document and document ID cannot be null");
        }
        
        String content = document.getContent();
        if (content == null || content.trim().isEmpty()) {
            // Skip documents without content
            return;
        }
        
        // Generate embedding for the document content
        List<Double> embedding = embeddingService.generateEmbedding(content);
        
        // Determine document type
        DocumentType docType = determineDocumentType(document);
        
        // Create vector document
        VectorDocument vectorDoc = new VectorDocument(
            document.getId(),
            document.getName(),
            content,
            docType,
            embedding
        );
        
        // Store in memory
        vectorStore.put(document.getId(), vectorDoc);
    }

    /**
     * Remove a document from the vector store
     * @param documentId The ID of the document to remove
     */
    public void removeDocument(Long documentId) {
        if (documentId != null) {
            vectorStore.remove(documentId);
        }
    }

    /**
     * Update a document in the vector store
     * @param document The updated document
     */
    public void updateDocument(Document document) {
        if (document != null && document.getId() != null) {
            // Remove old and add new
            removeDocument(document.getId());
            addDocument(document);
        }
    }

    /**
     * Find similar documents using vector similarity search
     * @param query The search query
     * @param topK Number of top results to return
     * @return List of similar documents sorted by similarity (highest first)
     */
    public List<VectorDocument> findSimilarDocuments(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        if (vectorStore.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Generate embedding for the query
        List<Double> queryEmbedding = embeddingService.generateQueryEmbedding(query);
        
        // Calculate similarity for all documents
        List<ScoredDocument> scoredDocuments = vectorStore.entrySet().stream()
                .map(entry -> {
                    double similarity = cosineSimilarity(queryEmbedding, entry.getValue().getEmbedding());
                    return new ScoredDocument(entry.getKey(), entry.getValue(), similarity);
                })
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity)) // Sort by similarity descending
                .limit(topK)
                .collect(Collectors.toList());
        
        return scoredDocuments.stream()
                .map(sd -> sd.vectorDocument)
                .collect(Collectors.toList());
    }

    /**
     * Find similar documents filtered by document type
     * @param query The search query
     * @param documentType The document type to filter by
     * @param topK Number of top results to return
     * @return List of similar documents of the specified type
     */
    public List<VectorDocument> findSimilarDocumentsByType(String query, DocumentType documentType, int topK) {
        List<VectorDocument> allSimilar = findSimilarDocuments(query, topK * 2); // Get more to filter
        
        return allSimilar.stream()
                .filter(doc -> doc.getDocumentType() == documentType)
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Calculate cosine similarity between two vectors
     * @param vector1 First vector
     * @param vector2 Second vector
     * @return Cosine similarity score (0 to 1, where 1 is most similar)
     */
    private double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }
        
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (denominator == 0.0) {
            return 0.0;
        }
        
        return dotProduct / denominator;
    }

    /**
     * Determine document type from document instance
     * @param document The document
     * @return DocumentType
     */
    private DocumentType determineDocumentType(Document document) {
        if (document instanceof com.knowledgehub.models.PolicyDocument) {
            return DocumentType.POLICY;
        } else if (document instanceof com.knowledgehub.models.ReportDocument) {
            return DocumentType.REPORT;
        } else if (document instanceof com.knowledgehub.models.ManualDocument) {
            return DocumentType.MANUAL;
        }
        return DocumentType.POLICY; // Default
    }

    /**
     * Get all documents in the vector store
     * @return Collection of all vector documents
     */
    public Collection<VectorDocument> getAllDocuments() {
        return vectorStore.values();
    }

    /**
     * Clear all documents from the vector store
     */
    public void clear() {
        vectorStore.clear();
    }

    /**
     * Get the number of documents in the vector store
     * @return Document count
     */
    public int size() {
        return vectorStore.size();
    }
    
    /**
     * Helper class to hold scored documents
     */
    private static class ScoredDocument {
        Long documentId;
        VectorDocument vectorDocument;
        double similarity;
        
        ScoredDocument(Long documentId, VectorDocument vectorDocument, double similarity) {
            this.documentId = documentId;
            this.vectorDocument = vectorDocument;
            this.similarity = similarity;
        }
    }
}

