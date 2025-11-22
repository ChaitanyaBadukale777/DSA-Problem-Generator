package com.example.dsaai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DsaQuestionResponse {
    private String title;
    private String problem_statement;
    private String constraints;
    private String example_input;
    private String example_output;
    private String topic;
}
