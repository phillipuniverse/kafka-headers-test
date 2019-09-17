package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;

import com.example.demo.DemoApplication.MessageRequest;
import com.example.demo.DemoApplication.MessageRequestListener;
import com.example.demo.DemoApplication.MessageRequestProducer;

@SpringBootTest
// the log.dir here avoids notwriteableexceptions that occur if this tries to write on a normal
// fs outside of the build directory
@EmbeddedKafka(brokerProperties = "log.dir=target/embedded-kafka")
@TestPropertySource(
        properties = {
                // bridge between embedded Kafka and Spring Cloud Stream
                "spring.cloud.stream.kafka.binder.brokers=${spring.embedded.kafka.brokers}",
                // using real kafka
                "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
                "spring.cloud.stream.bindings.messageRequestInput.group=consumer",
                "spring.cloud.stream.bindings.messageRequestInput.destination=messages",
                "spring.cloud.stream.bindings.messageRequestOutput.destination=messages",
                "spring.cloud.stream.kafka.default.consumer.standard-headers=both" // default prefix
                                                                                  // from
                                                                                  // org.springframework.cloud.stream.binder.kafka.properties.KafkaExtendedBindingProperties
        })
public class DemoApplicationTests {

    @SpyBean
    private MessageRequestListener listener;

    @Autowired
    private MessageRequestProducer producer;

    @Test
    public void messageIsReceived() {
        MessageRequest req = new MessageRequest("abc123");
        Message<MessageRequest> msg = MessageBuilder
                .withPayload(req)
                .setHeader("CUSTOM_HEADER", "val")
                .build();
        assertThat(msg.getHeaders().get(MessageHeaders.TIMESTAMP)).isNotNull();
        assertThat(msg.getHeaders().get(MessageHeaders.ID)).isNotNull();
        producer.messageRequestOutput().send(msg);

        verify(listener, timeout(2000)).handle(any());

        Message<MessageRequest> received = listener.getMessage();
        // custom
        assertThat(received.getPayload().getId()).isEqualTo(req.getId());
        assertThat(received.getHeaders().get("CUSTOM_HEADER")).isEqualTo("val");

        // should be automatic
        assertThat(received.getHeaders().get(MessageHeaders.TIMESTAMP))
                .withFailMessage("Could not find a TIMESTAMP header")
                .isNotNull();
        assertThat(received.getHeaders().get(MessageHeaders.ID))
                .withFailMessage("Could not find an ID header")

                .isNotNull();
    }

}
