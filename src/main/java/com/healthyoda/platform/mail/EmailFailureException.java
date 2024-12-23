package com.healthyoda.platform.mail;

import org.springframework.core.NestedRuntimeException;

public class EmailFailureException extends NestedRuntimeException {
    public EmailFailureException(String msg, Throwable e) {
        super(msg, e);
    }
}