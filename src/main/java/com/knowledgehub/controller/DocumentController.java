package com.knowledgehub.controller;

import java.io.IOException;
import java.util.List;
import com.knowledgehub.models.*;
import com.knowledgehub.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/allDocuments")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @PostMapping(value = "/uploadDocument", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is required and cannot be empty");
            }
            if (type == null) {
                return ResponseEntity.badRequest().body("Document type is required (POLICY, REPORT, or MANUAL)");
            }
            Document document = documentService.saveDocument(file, type);
            return ResponseEntity.ok(document);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error saving file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteDocument/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/getDocument/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable("id") Long id) {
        Document document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/getDocumentByName/{name}")
    public ResponseEntity<Document> getDocumentByName(@PathVariable("name") String name) {
        Document document = documentService.getDocumentByName(name);
        return ResponseEntity.ok(document);
    }
}
