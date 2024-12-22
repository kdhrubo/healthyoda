package com.healthyoda.platform.condition.ai;

import org.springframework.core.io.Resource;

public interface VoiceService {

  String transcribe(Resource audioFileResource);

  byte[] textToSpeech(String text);

}