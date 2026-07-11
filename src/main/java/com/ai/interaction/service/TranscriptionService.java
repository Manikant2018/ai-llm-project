package com.ai.interaction.service;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class TranscriptionService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public TranscriptionService(@Qualifier("groqTranscriptionModel") OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcribe(Resource audioFile) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile);
        return transcriptionModel.call(prompt).getResult().getOutput();
    }
}