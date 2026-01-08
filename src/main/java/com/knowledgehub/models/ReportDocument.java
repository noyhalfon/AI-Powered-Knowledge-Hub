package com.knowledgehub.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REPORT")
public class ReportDocument extends Document {

    @Column(nullable = false)
    private DocumentType type;

    @Override
    public void setType() {
        this.type = DocumentType.REPORT;
    }
    
    @Override
    public DocumentType getType() {
        return this.type;
    }

    @Override
    public String getSpecialInstruction() {
        return """

        You are a helpful assistant for the Knowledge Hub system.
        Answer the user's question based ONLY on the following document.
        Your answers must be strictly based on the specific sections and headings of the provided text.
        Prioritize the hierarchical structure of the document when formulating your response
        
        Document: %s
        
        Document Content:
        %s
        """.formatted(this.getName(), this.getContent());
    }
}