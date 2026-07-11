package com.ai.interaction.service;

import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SpeechService {

    private final OpenAiAudioSpeechModel speechModel;

    public SpeechService(@Qualifier("groqSpeechModel") OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    public byte[] speak(String text) {
        SpeechPrompt prompt = new SpeechPrompt(text);
        return speechModel.call(prompt).getResult().getOutput();
    }
}