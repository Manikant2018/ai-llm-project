package com.ai.interaction.controller;

import com.ai.interaction.service.SpeechService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Text-to-Speech (TTS) services using Groq Aura.
 */
@RestController
public class SpeechController {

    private final SpeechService speechService;

    public SpeechController(SpeechService speechService) {
        this.speechService = speechService;
    }

    @GetMapping("/ai/speak")
    public ResponseEntity<byte[]> speak(@RequestParam(value = "text", defaultValue = "Hello, I am Groq.") String text) {
        byte[] audioData = speechService.speak(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setContentLength(audioData.length);
        return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
    }
}