package com.healthyoda.platform.mail;


import java.util.Map;

public record Email(String from, String to, String subject, String templateName,
                    Map<String, Object> model) {
}