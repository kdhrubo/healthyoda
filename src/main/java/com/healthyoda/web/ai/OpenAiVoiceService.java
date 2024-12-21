package com.healthyoda.web.ai;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class OpenAiVoiceService implements VoiceService {

  private final OpenAiAudioTranscriptionModel transcriptionModel;

  public OpenAiVoiceService(
      OpenAiAudioTranscriptionModel transcriptionModel) {
    this.transcriptionModel = transcriptionModel; //
  }

  @Override
  public String transcribe(Resource audioFileResource) {
    return transcriptionModel.call(audioFileResource); //
  }

  @Override
  public byte[] textToSpeech(String text) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}