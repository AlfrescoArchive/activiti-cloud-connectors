package org.activiti.cloud.connectors.ranking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.cloud.connectors.starter.channels.CloudConnectorChannels;
import org.activiti.cloud.connectors.starter.model.IntegrationRequestEvent;
import org.activiti.cloud.connectors.starter.model.IntegrationResultEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TweetRankConnector {

    @Autowired
    private MessageChannel integrationResultsProducer;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.build();
    }

    @StreamListener(value = CloudConnectorChannels.INTEGRATION_EVENT_CONSUMER, condition = "headers['connectorType']=='Rank Tweet'")
    public synchronized void processEnglish(IntegrationRequestEvent event) throws InterruptedException {

      //  System.out.println("Just recieved an integration request event: " + event);

        String message = String.valueOf(event.getVariables().get("message"));

        System.out.println(">>> I should rank this tweet: " + message);

        Map<String, Object> results = new HashMap<>();

        IntegrationResultEvent ire = new IntegrationResultEvent(UUID.randomUUID().toString(),
                                                                event.getExecutionId(),
                                                                results);

        //System.out.println("I'm sending back an integratrion Result: " + ire);
        integrationResultsProducer.send(MessageBuilder.withPayload(ire).build());
    }
}
