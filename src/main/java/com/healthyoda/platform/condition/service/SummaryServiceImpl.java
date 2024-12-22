package com.healthyoda.platform.condition.service;

import com.healthyoda.platform.condition.ai.ChatService;
import com.healthyoda.platform.condition.ai.VoiceService;
import com.healthyoda.platform.condition.model.Question;
import com.healthyoda.platform.condition.model.TranscribedQuestionAnswer;
import com.healthyoda.platform.condition.repository.CoughQuestionnaireRepository;
import com.healthyoda.platform.condition.storage.AudioStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SummaryServiceImpl implements SummaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummaryServiceImpl.class);
    private final AudioStorageService audioStorageService;
    private final VoiceService voiceService;
    private final CoughQuestionnaireRepository coughQuestionnaireRepository;
    private final ChatService chatService;

    @Value("classpath:/prompt/condition_summary.st")
    Resource promptTemplateResource;


    public SummaryServiceImpl(AudioStorageService audioStorageService, VoiceService voiceService, CoughQuestionnaireRepository coughQuestionnaireRepository, ChatService chatService) {
        this.audioStorageService = audioStorageService;
        this.voiceService = voiceService;
        this.coughQuestionnaireRepository = coughQuestionnaireRepository;
        this.chatService = chatService;
    }


    @Async
    public void createSummary() {
        List<Resource> resourceList =
        audioStorageService.getAllFiles();

        LOGGER.info("Total files: {}", resourceList.size());

        //Now transcribe by calling open ai and note the texts

        int i = 0;

        List<Question> questionList = coughQuestionnaireRepository.getAllQuestions();

        StringBuilder conversation = new StringBuilder();

        for (Resource resource : resourceList) {
            String text =
            this.voiceService.transcribe(resource);

            LOGGER.info("Transcribed file: {}", text);

            TranscribedQuestionAnswer transcribedQuestionAnswer =
            new TranscribedQuestionAnswer(questionList.get(i).text(), text);

            conversation.append("\n").append(transcribedQuestionAnswer.getQA());
        }

        PromptTemplate userPromptTemplate = new PromptTemplate(promptTemplateResource);
        Message userMessage = userPromptTemplate.createMessage(Map.of("conversation", conversation));

        String summary = chatService.chat(userMessage);

        LOGGER.info("Summary: {}", summary);

    }

}
