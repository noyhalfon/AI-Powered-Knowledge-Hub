package com.knowledgehub.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskQuestionRequest {
    private String question;
    private String documentName; // Optional: if provided, ask about specific document
}

