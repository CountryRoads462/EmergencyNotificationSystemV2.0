package com.andrew.ens.google_smtp.application.port.in;

public interface SendEmailUseCase {
    void sendEmail(String subject, String body, String toEmail);
}
