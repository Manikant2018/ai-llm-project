package com.ai.interaction.controller;

import com.ai.interaction.service.TranscriptionService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller for Audio-to-Text Transcription services using Groq Whisper.
 */
@RestController
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/ai/transcribe")
    public ResponseEntity<Map<String, String>> transcribe(@RequestParam("file") MultipartFile file) {
        Resource audioResource = file.getResource();
        String transcription = transcriptionService.transcribe(audioResource);
        return ResponseEntity.ok(Map.of("transcription", transcription));
    }
}