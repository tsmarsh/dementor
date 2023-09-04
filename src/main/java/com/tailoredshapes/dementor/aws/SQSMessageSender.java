package com.tailoredshapes.dementor.aws;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.tailoredshapes.dementor.MessageSender;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SQSMessageSender implements MessageSender {

    private final SqsClient sqs;

    public SQSMessageSender(SqsClient sqs) {
        this.sqs = sqs;
    }

    @Override
    public void sendMessage(Long chatId, String message, String topic) {
        SendMessageRequest send_msg_request = SendMessageRequest.builder()
                .queueUrl(topic)
                .messageGroupId(chatId.toString())
                .messageBody(message)
                .build();

        sqs.sendMessage(send_msg_request);
    }
}
