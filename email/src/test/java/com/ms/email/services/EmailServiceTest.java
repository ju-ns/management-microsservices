package com.ms.email.services;

import com.ms.email.enums.StatusEmail;
import com.ms.email.models.EmailModel;
import com.ms.email.repositories.EmailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private JavaMailSender emailSender;

    @Test
    void shouldSendEmailSuccessfully(){
        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo("test@example.com");
        emailModel.setSubject("Teste");
        emailModel.setText("Conteúdo do e-mail");

        when(emailRepository.save(any(EmailModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailModel savedEmail = emailService.sendEmail(emailModel);

        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(emailRepository, times(1)).save(any(EmailModel.class));
        assertEquals(StatusEmail.SENT, savedEmail.getStatusEmail());
        assertNotNull(savedEmail.getSendDateEmail());
        assertEquals(emailService.getEmailFrom(), savedEmail.getEmailFrom());
    }
    @Test
    void shouldSetStatusErrorWhenMailException(){
        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo("test@example.com");
        emailModel.setSubject("Teste");
        emailModel.setText("Content email");

        doThrow(new MailException("Fail sending"){}).when(emailSender).send(any(SimpleMailMessage.class));
        when(emailRepository.save(any(EmailModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailModel savedEmail = emailService.sendEmail(emailModel);

        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(emailRepository, times(1)).save(any(EmailModel.class));
        assertEquals(StatusEmail.ERROR, savedEmail.getStatusEmail());
        assertNotNull(savedEmail.getSendDateEmail());
        assertEquals(emailService.getEmailFrom(), savedEmail.getEmailFrom());
    }
}
