package org.activiti.cloud.connectors.twitter;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.cloud.connectors.starter.channels.CloudConnectorChannels;
import org.activiti.cloud.connectors.starter.channels.ProcessRuntimeChannels;
import org.activiti.cloud.connectors.starter.configuration.EnableActivitiCloudConnector;
import org.activiti.cloud.connectors.starter.model.IntegrationRequestEvent;
import org.activiti.cloud.connectors.starter.model.IntegrationResultEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import org.activiti.cloud.services.core.model.commands.StartProcessInstanceCmd;

@SpringBootApplication
@EnableActivitiCloudConnector
@ComponentScan("org.activiti.cloud.connectors.starter")
public class TwitterCloudConnector implements CommandLineRunner {

    @Autowired
    private MessageChannel integrationResultsProducer;

    @Autowired
    private MessageChannel runtimeCmdProducer;

    public static void main(String[] args) {
        SpringApplication.run(TwitterCloudConnector.class,
                              args);
    }

    @Override
    public void run(String... args) throws Exception {
        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {
                //Start a process
                System.out.println("> Tweet: " + status.getText()+"\n");
                System.out.println("\t > Lang: " + status.getLang()+"\n");
                Map<String, Object> vars = new HashMap<>();
                vars.put("message",status.getText());

                StartProcessInstanceCmd startProcessInstanceCmd = new StartProcessInstanceCmd("tweet-processor:1:3",
                                                                      vars);
                runtimeCmdProducer.send(MessageBuilder.withPayload(startProcessInstanceCmd).build());
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            @Override
            public void onScrubGeo(long l,
                                   long l1) {
                System.out.println("Long: " + l + " - Lat: " + l1);
            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);
        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.sample();
    }

    @StreamListener(value = CloudConnectorChannels.INTEGRATION_EVENT_CONSUMER, condition = "headers['connectorType']=='Twitter Tweet'")
    public void tweet(IntegrationRequestEvent event) {

        System.out.println("Just recieved an integration request event: " + event);
        Map<String, Object> results = new HashMap<>();
        results.put("flagged", true);
        System.out.println("\t Variables: " + event.getVariables().size());
        System.out.println("\t>>>>>>> I'm tweeting: "+ event.getVariables().get("message") + " \n");

        IntegrationResultEvent ire = new IntegrationResultEvent(UUID.randomUUID().toString(),
                                                                event.getExecutionId(),
                                                                results);

        System.out.println("I'm sending back an integratrion Result: " + ire);
        integrationResultsProducer.send(MessageBuilder.withPayload(ire).build());
    }
}
