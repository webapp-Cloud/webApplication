package com.rio_rishabhNEU.UserApp.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import org.json.JSONObject;

@Service
public class SNSService {
    private static final Logger logger = LoggerFactory.getLogger(SNSService.class);
    private final SnsClient snsClient;
    private final String topicArn;

    public SNSService(@Value("${aws.sns.topic.arn}") String topicArn) {
        this.snsClient = SnsClient.builder().build();
        this.topicArn = topicArn;
        logger.info("SNSService initialized with topic ARN: {}", topicArn);
    }

    public void publishUserCreationMessage(String email, String firstName, String verificationToken) {
        try {
            JSONObject message = new JSONObject();
            message.put("email", email);
            message.put("firstName", firstName);
            message.put("verificationToken", verificationToken);

            PublishRequest request = PublishRequest.builder()
                    .message(message.toString())
                    .topicArn(topicArn)
                    .build();

            PublishResponse response = snsClient.publish(request);
            logger.info("Message published to SNS. MessageId: {}", response.messageId());
        } catch (Exception e) {
            logger.error("Failed to publish message to SNS", e);
            throw new RuntimeException("Failed to publish user creation message", e);
        }
    }
}