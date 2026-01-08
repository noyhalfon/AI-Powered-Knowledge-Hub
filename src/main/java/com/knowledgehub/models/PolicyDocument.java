package com.knowledgehub.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("POLICY")
public class PolicyDocument extends Document {

    @Column(nullable = false)
    private DocumentType type;

    @Override
    public void setType() {
        this.type = DocumentType.POLICY;
    }
    
    @Override
    public DocumentType getType() {
        return this.type;
    }

    @Override
    public String getSpecialInstruction() {
        return """

        Your answers must be strictly based on the specific sections and headings of the provided documents. 
        When responding, you should include concise summaries and highlight key data points where relevant to ensure professional clarity and structured insight.
        
        Document: %s
        
        Document Content:
        %s
        """.formatted(this.getName(), this.getContent());
    }
}