package com.healthyoda.platform.mail;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResendEmailService extends EmailService {

    private final Logger LOGGER = LoggerFactory.getLogger(ResendEmailService.class);

    private final Resend resend;

    public ResendEmailService(Resend resend) {
        this.resend = resend;
    }

    @Override
    public void sendEmail(Email email) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(email.from())
                .to(email.to())
                .subject(email.subject())
                .html(this.buildBody(email))
                .build();
        try {
            CreateEmailResponse data = resend.emails().send(params);
            LOGGER.info(data.getId());
        } catch (ResendException e) {
            LOGGER.info("Error sending email", e);
        }
    }


}