package com.healthyoda.platform.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


public abstract class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private SpringTemplateEngine templateEngine;

    public abstract void sendEmail(Email email);

    protected String buildBody(Email email) {
        Context context = new Context();
        context.setVariables(email.model());
        String html = this.templateEngine.process(email.templateName(), context);

        LOGGER.debug("html - {}", html);

        return html;
    }

    @Autowired
    public void setSpringTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
}
