package com.healthyoda.web.summary;

import com.healthyoda.web.AudioStorageService;

import com.healthyoda.web.ai.VoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryServiceImpl implements SummaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummaryServiceImpl.class);
    private final AudioStorageService audioStorageService;
    private final VoiceService voiceService;

    public SummaryServiceImpl(AudioStorageService audioStorageService, VoiceService voiceService) {
        this.audioStorageService = audioStorageService;
        this.voiceService = voiceService;
    }


    @Async
    public void createSummary() {
        List<Resource> resourceList =
        audioStorageService.getAllFiles();

        LOGGER.info("Total files: {}", resourceList.size());

        //Now transcribe by calling open ai and note the texts

        for (Resource resource : resourceList) {
            String text =
            this.voiceService.transcribe(resource);

            LOGGER.info("Transcribed file: {}", text);
        }


    }

}
