package com.knowledgehub.config;

import com.knowledgehub.models.Document;
import com.knowledgehub.repositories.DocumentRepository;
import com.knowledgehub.services.VectorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes the vector store with existing documents on application startup
 */
@Component
public class VectorStoreInitializer implements CommandLineRunner {

    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private VectorStoreService vectorStoreService;

    @Override
    public void run(String... args) throws Exception {
        // Load all existing documents into vector store
        List<Document> allDocuments = documentRepository.findAll();
        
        System.out.println("Initializing vector store with " + allDocuments.size() + " documents...");
        
        for (Document doc : allDocuments) {
            try {
                vectorStoreService.addDocument(doc);
            } catch (Exception e) {
                System.err.println("Warning: Failed to add document " + doc.getId() + " to vector store: " + e.getMessage());
            }
        }
        
        System.out.println("Vector store initialized with " + vectorStoreService.size() + " documents.");
    }
}

