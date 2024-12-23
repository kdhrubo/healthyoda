package com.healthyoda.platform.condition.service;

import com.healthyoda.platform.condition.ai.ChatService;
import com.healthyoda.platform.condition.ai.VoiceService;
import com.healthyoda.platform.condition.model.Question;
import com.healthyoda.platform.condition.model.TranscribedQuestionAnswer;
import com.healthyoda.platform.condition.repository.CoughQuestionnaireRepository;
import com.healthyoda.platform.condition.storage.AudioStorageService;
import com.healthyoda.platform.mail.Email;
import com.healthyoda.platform.mail.EmailService;
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
    private final EmailService emailService;

    @Value("classpath:/prompt/condition_summary.st")
    Resource promptTemplateResource;


    public SummaryServiceImpl(AudioStorageService audioStorageService, VoiceService voiceService, CoughQuestionnaireRepository coughQuestionnaireRepository, ChatService chatService, EmailService emailService) {
        this.audioStorageService = audioStorageService;
        this.voiceService = voiceService;
        this.coughQuestionnaireRepository = coughQuestionnaireRepository;
        this.chatService = chatService;
        this.emailService = emailService;
    }



    public void createSummary(String sId) {
        List<Resource> resourceList = audioStorageService.getAllFiles();

        LOGGER.info("Total files: {}", resourceList.size());

        //Now transcribe by calling open ai and note the texts

        int i = 0;

        List<Question> questionList = coughQuestionnaireRepository.getAllQuestions();

        StringBuilder conversation = new StringBuilder();

        for (Resource resource : resourceList) {
            String text =
            this.voiceService.transcribe(resource);

            TranscribedQuestionAnswer transcribedQuestionAnswer =
            new TranscribedQuestionAnswer(questionList.get(i++).text(), text);

            conversation.append("\n").append(transcribedQuestionAnswer.getQA());
        }

        LOGGER.info("conversation: {}", conversation);

        PromptTemplate userPromptTemplate = new PromptTemplate(promptTemplateResource);
        Message userMessage = userPromptTemplate.createMessage(Map.of("conversation", conversation));

        String summary = chatService.chat(userMessage);

        LOGGER.info("Summary: {}", summary);

        var email = new Email("info@db2rest.com",
                "grabdoc2020@gmail.com",
                "Appointment # " + sId + " Patient Response Summary" ,
                "email-summary",
                Map.of("appointmentNo", sId, "portalUrl", "https://portal.healthyoda.com"
                        , "summary", summary
                )
        );

        emailService.sendEmail(email);

    }

}
