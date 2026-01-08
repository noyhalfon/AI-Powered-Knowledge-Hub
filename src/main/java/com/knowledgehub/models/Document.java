package com.knowledgehub.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "documents")
public abstract class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String path;
    
    @Column(nullable = false)
    private Long size;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public abstract String getSpecialInstruction(); 

    public abstract DocumentType getType();

    public abstract void setType();
}
