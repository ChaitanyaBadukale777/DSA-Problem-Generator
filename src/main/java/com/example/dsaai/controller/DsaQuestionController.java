package com.example.dsaai.controller;

import com.example.dsaai.model.DsaQuestionResponse;
import com.example.dsaai.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DsaQuestionController {

    private final GeminiService geminiService;

    public DsaQuestionController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/generate-dsa-question")
    public DsaQuestionResponse generate(@RequestParam String level) {
        return geminiService.generateQuestion(level);
    }
}
