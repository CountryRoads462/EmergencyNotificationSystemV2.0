package com.andrew.ens.google_smtp.application;

import com.andrew.ens.google_smtp.application.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService implements SendEmailUseCase {

    @Autowired
    private final JavaMailSender mailSender;

    @Autowired
    private final Environment env;

    @Override
    public void sendEmail(String subject, String body, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(env.getProperty("spring.mail.username"));
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);
    }
}
