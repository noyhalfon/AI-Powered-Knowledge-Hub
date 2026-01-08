package com.knowledgehub.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANUAL")
public class ManualDocument extends Document {

    @Column(nullable = false)
    private DocumentType type;

    @Override
    public void setType() {
        this.type = DocumentType.MANUAL;
    }
    
    @Override
    public DocumentType getType() {
        return this.type;
    }

    @Override
    public String getSpecialInstruction() {
        return """
        
        Your answers must be strictly based on the specific sections and headings of the provided documents. 
        When providing guidance or 'how-to' information from a Manual, you MUST format your response as a numbered list (1, 2, 3...). 
        For Reports, include concise summaries and use bold text to highlight key data points.
        
        Document: %s
        
        Document Content:
        %s
        """.formatted(this.getName(), this.getContent());
    }
}