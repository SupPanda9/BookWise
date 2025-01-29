package com.bookwise.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            // Email Subject and Content
            String subject = "Verify Your Email Address";
            String message = "Click the following link to verify your email address:\n" + verificationLink;

            // Create the email
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(toEmail);
            email.setSubject(subject);
            email.setText(message);
            email.setFrom("no-reply@bookwise.com");

            // Send the email
            mailSender.send(email);
            System.out.println("Verification email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}
