package com.knowledgehub.services;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.knowledgehub.models.Document;
import com.knowledgehub.models.DocumentType;
import com.knowledgehub.models.ManualDocument;
import com.knowledgehub.models.PolicyDocument;
import com.knowledgehub.models.ReportDocument;
import com.knowledgehub.models.VectorDocument;
import com.knowledgehub.repositories.DocumentRepository;

@Service
public class AIService {

    private final ChatClient chatClient;
    private final DocumentRepository documentRepository;
    private final VectorStoreService vectorStoreService;
    
    // Number of top similar documents to retrieve
    private static final int TOP_K_DOCUMENTS = 5;

    @Autowired
    public AIService(Builder chatClientBuilder, DocumentRepository documentRepository, 
                     VectorStoreService vectorStoreService) {
        this.chatClient = chatClientBuilder.build();
        this.documentRepository = documentRepository;
        this.vectorStoreService = vectorStoreService;
    }

    public String askAboutDocuments(String userMessage) {
        // Extract document name from message if mentioned
        String documentName = extractDocumentNameFromMessage(userMessage);
        
        if (documentName != null) {
            // Ask about specific document using vector search for better context
            List<Document> documents = documentRepository.findByNameContainingIgnoreCase(documentName);
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("Document not found with name: " + documentName);
            }
            Document doc = documents.get(0);
            String documentContent = doc.getContent();
            if (documentContent == null || documentContent.trim().isEmpty()) {
                throw new IllegalArgumentException("Document '" + documentName + "' has no content.");
            }
            
            // Use vector search to find most relevant parts of the document
            List<com.knowledgehub.models.VectorDocument> similarChunks = 
                vectorStoreService.findSimilarDocuments(userMessage + " " + documentName, 3);
            
            // Use polymorphic behavior - call getSpecialInstruction() for the specific document type
            String specialInstruction = doc.getSpecialInstruction();
            
            String systemPrompt;
            if (specialInstruction != null && !specialInstruction.trim().isEmpty()) {
                // Use the special instruction from the document type
                systemPrompt = specialInstruction;
            } else {
                // Default prompt if no special instruction
                String relevantContent = similarChunks.isEmpty() 
                    ? documentContent 
                    : similarChunks.stream()
                        .map(vd -> vd.getContent())
                        .collect(Collectors.joining("\n\n---\n\n"));
                        
                systemPrompt = """
                    You are a helpful assistant for the Knowledge Hub system. 
                    Answer the user's question based ONLY on the following document.
                    If the answer is not in the document, clearly state that the information is not available.
                    
                    Document: %s
                    
                    Relevant Document Content:
                    %s
                    """.formatted(doc.getName(), relevantContent);
            }
            
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        }
        
        // Use vector search to find most relevant documents
        List<VectorDocument> similarDocs = 
            vectorStoreService.findSimilarDocuments(userMessage, TOP_K_DOCUMENTS);
        
        if (similarDocs.isEmpty()) {
            // Fallback to traditional search if vector store is empty
            return askAboutDocumentsFallback(userMessage);
        }
        
        // Group by document type
        List<VectorDocument> policyDocs = similarDocs.stream()
            .filter(vd -> vd.getDocumentType() == DocumentType.POLICY)
            .collect(Collectors.toList());
        List<VectorDocument> manualDocs = similarDocs.stream()
            .filter(vd -> vd.getDocumentType() == DocumentType.MANUAL)
            .collect(Collectors.toList());
        List<VectorDocument> reportDocs = similarDocs.stream()
            .filter(vd -> vd.getDocumentType() == DocumentType.REPORT)
            .collect(Collectors.toList());
        
        // Build content from vector search results
        String policyContent = formatVectorDocuments(policyDocs);
        String manualContent = formatVectorDocuments(manualDocs);
        String reportContent = formatVectorDocuments(reportDocs);
        
        // Get special instructions from the retrieved documents
        StringBuilder specialInstructions = new StringBuilder();
        for (VectorDocument vd : similarDocs) {
            Document doc = documentRepository.findById(vd.getDocumentId()).orElse(null);
            if (doc != null) {
                String instruction = doc.getSpecialInstruction();
                if (instruction != null && !instruction.trim().isEmpty()) {
                    specialInstructions.append(instruction).append("\n\n");
                }
            }
        }
    
        String systemPrompt = """
            You are an expert organizational assistant. Answer the user's question based ONLY on the provided documents.
            
            RULES FOR RESPONDING BY DOCUMENT TYPE:
            1. If the information comes from a POLICY: Answer in a formal, authoritative tone. Start with "According to organization policy...".
            2. If the information comes from a MANUAL: Answer in a step-by-step, technical manner.
            3. If the information comes from a REPORT: Focus on data, dates, and figures. Be concise and objective.
            
            %s
            ---
            RELEVANT POLICY DOCUMENTS (found via semantic search):
            %s
            
            RELEVANT MANUAL DOCUMENTS (found via semantic search):
            %s
            
            RELEVANT REPORT DOCUMENTS (found via semantic search):
            %s
            """.formatted(
                specialInstructions.toString().trim(),
                policyContent.isEmpty() ? "No relevant policy documents found." : policyContent,
                manualContent.isEmpty() ? "No relevant manual documents found." : manualContent,
                reportContent.isEmpty() ? "No relevant report documents found." : reportContent
            );
    
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
    
    /**
     * Fallback method when vector store is empty (uses traditional full-text search)
     */
    private String askAboutDocumentsFallback(String userMessage) {
        List<Document> allDocs = documentRepository.findAll();
        
        // Get content and special instructions for each document type (polymorphic behavior)
        DocumentTypeContent policyData = getContentAndInstructionsByType(allDocs, DocumentType.POLICY);
        DocumentTypeContent manualData = getContentAndInstructionsByType(allDocs, DocumentType.MANUAL);
        DocumentTypeContent reportData = getContentAndInstructionsByType(allDocs, DocumentType.REPORT);
    
        // Build special instructions section from all document types
        StringBuilder specialInstructions = new StringBuilder();
        if (!policyData.specialInstructions.isEmpty()) {
            specialInstructions.append("POLICY DOCUMENTS SPECIAL INSTRUCTIONS:\n")
                              .append(policyData.specialInstructions)
                              .append("\n\n");
        }
        if (!manualData.specialInstructions.isEmpty()) {
            specialInstructions.append("MANUAL DOCUMENTS SPECIAL INSTRUCTIONS:\n")
                              .append(manualData.specialInstructions)
                              .append("\n\n");
        }
        if (!reportData.specialInstructions.isEmpty()) {
            specialInstructions.append("REPORT DOCUMENTS SPECIAL INSTRUCTIONS:\n")
                              .append(reportData.specialInstructions)
                              .append("\n\n");
        }
    
        String systemPrompt = """
            You are an expert organizational assistant. Answer the user's question based ONLY on the provided documents.
            
            RULES FOR RESPONDING BY DOCUMENT TYPE:
            1. If the information comes from a POLICY: Answer in a formal, authoritative tone. Start with "According to organization policy...".
            2. If the information comes from a MANUAL: Answer in a step-by-step, technical manner.
            3. If the information comes from a REPORT: Focus on data, dates, and figures. Be concise and objective.
            
            %s
            ---
            POLICY DOCUMENTS:
            %s
            
            MANUAL DOCUMENTS:
            %s
            
            REPORT DOCUMENTS:
            %s
            """.formatted(
                specialInstructions.toString().trim(),
                policyData.content,
                manualData.content,
                reportData.content
            );
    
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
    
    /**
     * Format vector documents for prompt
     */
    private String formatVectorDocuments(List<com.knowledgehub.models.VectorDocument> vectorDocs) {
        return vectorDocs.stream()
            .map(vd -> "- " + vd.getDocumentName() + ":\n" + vd.getContent())
            .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    /**
     * Extract document name from user message by checking if any document name appears in the message
     * @param message User's message
     * @return Document name if found, null otherwise
     */
    private String extractDocumentNameFromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        List<Document> allDocuments = documentRepository.findAll();
        String lowerMessage = message.toLowerCase();
        
        for (Document doc : allDocuments) {
            String docName = doc.getName();
            if (docName != null) {
                String lowerDocName = docName.toLowerCase();
                
                // Check for full name match
                if (lowerMessage.contains(lowerDocName)) {
                    return docName;
                }
                
                // Check for filename without extension
                if (lowerDocName.contains(".")) {
                    String nameWithoutExt = lowerDocName.substring(0, lowerDocName.lastIndexOf("."));
                    if (lowerMessage.contains(nameWithoutExt) && nameWithoutExt.length() > 3) {
                        return docName;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Helper class to hold content and special instructions for a document type
     */
    private static class DocumentTypeContent {
        String content;
        String specialInstructions;
        
        DocumentTypeContent(String content, String specialInstructions) {
            this.content = content;
            this.specialInstructions = specialInstructions;
        }
    }
    
    /**
     * Get content and special instructions for documents of a specific type (polymorphic behavior)
     * Calls getSpecialInstruction() for each document type
     */
    private DocumentTypeContent getContentAndInstructionsByType(List<Document> docs, DocumentType type) {
        List<Document> filteredDocs = docs.stream()
                .filter(d -> {
                    if (type == DocumentType.POLICY) {
                        return d instanceof PolicyDocument;
                    } else if (type == DocumentType.MANUAL) {
                        return d instanceof ManualDocument;
                    } else if (type == DocumentType.REPORT) {
                        return d instanceof ReportDocument;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Build content string
        String content = filteredDocs.stream()
                .map(d -> {
                    String docContent = d.getContent();
                    return docContent != null && !docContent.trim().isEmpty() 
                        ? "- " + d.getName() + ": " + docContent 
                        : "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));
        
        // Build special instructions using polymorphic getSpecialInstruction() call
        String specialInstructions = filteredDocs.stream()
                .map(d -> {
                    // Polymorphic call - each document type implements getSpecialInstruction() differently
                    String instruction = d.getSpecialInstruction();
                    return instruction != null && !instruction.trim().isEmpty() ? instruction : "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n\n"));
        
        return new DocumentTypeContent(content, specialInstructions);
    }
 
}
