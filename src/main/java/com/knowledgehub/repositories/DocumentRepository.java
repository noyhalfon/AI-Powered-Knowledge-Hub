package com.knowledgehub.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.knowledgehub.models.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    // ========== Inherited from JpaRepository ==========
    // These methods are automatically available:
    // - save(Document entity) - Save or update a document
    // - findById(Long id) - Find document by ID
    // - findAll() - Find all documents
    // - deleteById(Long id) - Delete document by ID
    // - delete(Document entity) - Delete a document
    // - count() - Count all documents
    // - existsById(Long id) - Check if document exists by ID
    // ===================================================
    
    // Find document by name
    Optional<Document> findByName(String name);
    
    // Find documents by name containing (case-insensitive)
    List<Document> findByNameContainingIgnoreCase(String name);
    
    // Custom query to find documents by name pattern
    @Query("SELECT d FROM Document d WHERE d.name LIKE %:pattern%")
    List<Document> findByNamePattern(@Param("pattern") String pattern);
    
}
