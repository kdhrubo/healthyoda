package com.healthyoda.platform.mail;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfiguration {

    @Bean
    public Resend resend(@Value("${RESEND_API_KEY}") String resendApiKey) {
        return new Resend(resendApiKey);
    }
}