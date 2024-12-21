package com.healthyoda.web;

import com.healthyoda.web.repository.CoughQuestionnaireRepository;
import com.healthyoda.web.repository.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RequestMapping("/session")
public class QuestionnaireController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireController.class);

    @Autowired
    private CoughQuestionnaireRepository repository;

    @Autowired
    private AudioService audioService;



    @GetMapping
    public String showLanding(
            @RequestParam String sId,
            Model model) {

        System.out.println("APPOINTMENT ID: " + sId);
        model.addAttribute("sId", sId);
        model.addAttribute("landed", true);
        return "home";
    }

    @PostMapping("/start")
    public String startSession(
            @RequestParam("sId") String sId,
                             @RequestParam("questionId") int questionId,
                             Model model) {

        System.out.println("questionRequest: " + questionId);

        int nextQuestionId = questionId + 1;

        // Get first question
        Question question = repository.getNextQuestion(nextQuestionId);
        model.addAttribute("question", question);
        model.addAttribute("questionId", nextQuestionId);
        model.addAttribute("totalQuestions", repository.getTotalQuestions());
        model.addAttribute("sId", sId);

        model.addAttribute("landed", false);

        return "home";
    }

    @PostMapping("/upload")
    public String handleAudioUpload(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("sId") String sId,
            @RequestParam("questionId") int questionId,
            Model model) {
        
        try {
            // Generate unique filename and save audio
            String fileName = sId + "_q" + questionId + "_" + UUID.randomUUID() + ".wav";
            audioService.saveAudio(audioFile, fileName);

            int nextQuestionId = questionId + 1;

            // Get next question
            Question nextQuestion = repository.getNextQuestion(nextQuestionId);


            
            if (nextQuestion != null) {
                model.addAttribute("question", nextQuestion);
                model.addAttribute("questionId", nextQuestionId);
                model.addAttribute("totalQuestions", repository.getTotalQuestions());
                model.addAttribute("sId", sId);
                model.addAttribute("landed", false);
                return "home";
            } else {
                // No more questions, redirect to completion page
                return "redirect:/session/complete?sId=" + sId;
            }
            
        } catch (Exception e) {
            LOGGER.error("Error processing audio upload - " ,  e);
            // Handle error appropriately
            return "error";
        }
    }

    @GetMapping("/complete")
    public String showCompletion(@RequestParam String sId, Model model) {
        // Get appointment details

        return "complete";
    }
}