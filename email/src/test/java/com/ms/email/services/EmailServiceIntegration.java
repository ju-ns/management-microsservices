package com.ms.email.services;

import com.ms.email.enums.StatusEmail;
import com.ms.email.models.EmailModel;
import com.ms.email.repositories.EmailRepository;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.mail.test-connection=false"
})

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EmailServiceIntegration {
    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailRepository emailRepository;

    @MockitoBean
    private JavaMailSender emailSender;

    @Test
    void shouldPersistEmailWithStatusSent(){
        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo("integration@example.com");
        emailModel.setSubject("subject");
        emailModel.setText("email message");

        EmailModel result = emailService.sendEmail(emailModel);

        assertEquals(StatusEmail.SENT, result.getStatusEmail());
        assertNotNull(result.getSendDateEmail());
        assertEquals(1, emailRepository.count());
    }

    @Test
    void shouldPersistEmailWithStatusErrorOnFailure(){
        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo("error@example.com");
        emailModel.setSubject("subject");
        emailModel.setText("error message");

        doThrow(new MailException("Error simulation") {
        }).when(emailSender).send(any(SimpleMailMessage.class));

        EmailModel result = emailService.sendEmail(emailModel);

        assertEquals(StatusEmail.ERROR, result.getStatusEmail());
        assertNotNull(result.getSendDateEmail());
        assertEquals(1, emailRepository.count());
    }

}
