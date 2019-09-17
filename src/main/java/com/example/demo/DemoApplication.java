package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

import com.example.demo.DemoApplication.MessageRequestConsumer;
import com.example.demo.DemoApplication.MessageRequestProducer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


@EnableBinding({MessageRequestProducer.class, MessageRequestConsumer.class})
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    public static interface MessageRequestProducer {
        public static final String CHANNEL = "messageRequestOutput";

        @Output(CHANNEL)
        MessageChannel messageRequestOutput();
    }

    public static interface MessageRequestConsumer {
        public static final String CHANNEL = "messageRequestInput";

        @Input(CHANNEL)
        SubscribableChannel messageRequestInput();
    }

    @Component
    public static class MessageRequestListener {
        private Message<MessageRequest> msg;

        @StreamListener(MessageRequestConsumer.CHANNEL)
        public void handle(Message<MessageRequest> msg) {
            this.msg = msg;
        }

        public Message<MessageRequest> getMessage() {
            return msg;
        }

    }

    public static class MessageRequest {
        private String id;

        @JsonCreator
        public MessageRequest(@JsonProperty("id") String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }
}
