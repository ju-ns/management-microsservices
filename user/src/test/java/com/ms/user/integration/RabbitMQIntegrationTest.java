package com.ms.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.user.dtos.EmailDto;
import com.ms.user.dtos.UserRecordDto;
import com.ms.user.exceptions.InvalidUserDataException;
import com.ms.user.models.UserModel;
import com.ms.user.producers.UserProducer;
import com.ms.user.services.UserService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.core.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.awaitility.Awaitility.await;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RabbitMQIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.12-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureRabbitMQProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getMappedPort(5672));
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserProducer userProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${broker.queue.email.name}")
    private String queueName;

    @Autowired
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUpQueue(){
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(new Queue(queueName, true));
    }

    public record TestEmailDto(
            UUID userId,
            String emailTo,
            String subject,
            String text
    ){}

    @Test
    void shouldPublishMessageToRabbitQueue() throws Exception {
        TestEmailDto payload = new TestEmailDto(
                UUID.randomUUID(),
                "test@email.com",
                "Test",
                "This is a test message"
        );

        String jsonPayload = objectMapper.writeValueAsString(payload);
        System.out.println("JSON sent: " + jsonPayload);

        rabbitTemplate.convertAndSend(queueName, jsonPayload);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String receivedJson = (String) rabbitTemplate.receiveAndConvert(queueName);
            assertNotNull(receivedJson, "The message should be in the queue");
            System.out.println("JSON received: " + receivedJson);

            TestEmailDto receivedPayload = objectMapper.readValue(receivedJson, TestEmailDto.class);
            assertEquals(payload.emailTo(), receivedPayload.emailTo());
            assertEquals(payload.subject(), receivedPayload.subject());
        });
    }
    @Test
    void shouldNotPublishMessageWhenEmailIsNull() {
        UserModel userModel = new UserModel();
        userModel.setName("Invalid User");
        userModel.setEmail(null);
        assertThrows(InvalidUserDataException.class, () -> {
            userService.save(userModel);
        });
        Message message = rabbitTemplate.receive(queueName, 1000);
        assertNull(message, "No message should be publish with null email");
    }

}
