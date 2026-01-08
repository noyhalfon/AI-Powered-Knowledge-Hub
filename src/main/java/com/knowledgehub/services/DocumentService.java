package com.knowledgehub.services;

import com.knowledgehub.models.*;
import com.knowledgehub.repositories.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private VectorStoreService vectorStoreService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDirectory;

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Document saveDocument(MultipartFile file, DocumentType type) throws IOException {
        ValidateFile(file, type);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(originalFilename);
        Files.write(filePath, file.getBytes());
        
        // Create document entity based on type
        Document doc;
        switch (type) {
            case POLICY:
                doc = new PolicyDocument();
                break;
            case REPORT:
                doc = new ReportDocument();
                break;
            case MANUAL:
                doc = new ManualDocument();
                break;
                default:
                    throw new IllegalArgumentException("Unknown document type");
                }
                        
        String content = extractTextFromWordFile(filePath, originalFilename.toLowerCase());
        doc = CreateDocument(doc, originalFilename, content, filePath, file.getSize());
        // Save to database
        Document savedDoc = documentRepository.save(doc);
        
        // Add to vector store for semantic search (async in production)
        try {
            vectorStoreService.addDocument(savedDoc);
        } catch (Exception e) {
            // Log error but don't fail document save
            System.err.println("Warning: Failed to add document to vector store: " + e.getMessage());
        }
        
        return savedDoc;
    }

    public void deleteDocument(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        
        // Get document to delete the file
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (documentOpt.isPresent()) {
            Document document = documentOpt.get();
            
            // Delete file from filesystem
            if (document.getPath() != null) {
                Path filePath = Paths.get(document.getPath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }
        }
        
        // Remove from vector store
        try {
            vectorStoreService.removeDocument(id);
        } catch (Exception e) {
            System.err.println("Warning: Failed to remove document from vector store: " + e.getMessage());
        }
        
        // Delete from database
        documentRepository.deleteById(id);
    }

    public Document getDocument(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        Optional<Document> document = documentRepository.findById(id);
        return document.orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
    }

    public Document getDocumentByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Document name cannot be null or empty");
        }
        Optional<Document> document = documentRepository.findByName(name);
        return document.orElseThrow(() -> new IllegalArgumentException("Document not found with name: " + name));
    }

    private void ValidateFile(MultipartFile file, DocumentType type) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Document type cannot be null");
        }
        
        // Validate file is a Word document
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        String lowerFilename = originalFilename.toLowerCase();
        if (!lowerFilename.endsWith(".doc") && !lowerFilename.endsWith(".docx")) {
            throw new IllegalArgumentException("Only Word documents (.doc, .docx) are allowed");
        }
    }

    private Document CreateDocument(Document doc, String originalFilename, String content, Path filePath, Long size) {
        doc.setName(originalFilename);
        doc.setContent(content);
        doc.setPath(filePath.toString());
        doc.setSize(size);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setType();

        return doc;
    }
    /**
     * Extract text content from Word document (.doc or .docx)
     * @param filePath Path to the Word document file
     * @param filename Lowercase filename to determine file type
     * @return Extracted text content from the document
     * @throws IOException if file cannot be read
     */
    private String extractTextFromWordFile(Path filePath, String filename) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            if (filename.endsWith(".docx")) {
                // Handle .docx files (Office Open XML format)
                try (XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            } else if (filename.endsWith(".doc")) {
                // Handle .doc files (older binary format)
                try (HWPFDocument document = new HWPFDocument(inputStream);
                     WordExtractor extractor = new WordExtractor(document)) {
                    return extractor.getText();
                }
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }
        } catch (Exception e) {
            throw new IOException("Error extracting text from Word document: " + e.getMessage(), e);
        }
    }
}