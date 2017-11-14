package org.activiti.cloud.connectors.twitter;

import java.util.HashMap;
import java.util.Map;

import org.activiti.cloud.connectors.starter.configuration.EnableActivitiCloudConnector;
import org.activiti.cloud.services.api.commands.StartProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

@SpringBootApplication
@EnableActivitiCloudConnector
@ComponentScan({"org.activiti.cloud.connectors.starter", "org.activiti.cloud.connectors.twitter"})
public class TwitterCloudConnectorApp implements CommandLineRunner {

    @Autowired
    private MessageChannel runtimeCmdProducer;


    public static void main(String[] args) {
        SpringApplication.run(TwitterCloudConnectorApp.class,
                              args);
    }

    @Override
    public void run(String... args) throws Exception {

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                //Start a process

                Map<String, Object> vars = new HashMap<>();
                String lang = status.getLang();
                vars.put("message",
                         status.getText());
                vars.put("lang",
                         lang);
                vars.put("user",
                         status.getUser().getName());
                vars.put("location",
                         status.getUser().getLocation());
                if (lang.equals("en")) {
                    System.out.println("> Tweet: " + status.getText() + "\n");
                    System.out.println("\t > Lang: " + status.getLang() + "\n");
                    StartProcessInstanceCmd startProcessInstanceCmd = new StartProcessInstanceCmd("tweet-processor:1:0c448226-c95c-11e7-a336-e1525a47089b",
                                                                                                  vars);
                    runtimeCmdProducer.send(MessageBuilder.withPayload(startProcessInstanceCmd).build());
                }
//                else if (lang.equals("es")) {
//
//                    StartProcessInstanceCmd startProcessInstanceCmd = new StartProcessInstanceCmd("tweet-processor:2:1568a104-c929-11e7-ae2c-f77ee315092b",
//                                                                                                  vars);
//                    runtimeCmdProducer.send(MessageBuilder.withPayload(startProcessInstanceCmd).build());
//                }
                else {
                    //System.out.println("No Process Defined for language: " + lang);
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            @Override
            public void onScrubGeo(long l,
                                   long l1) {
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
}
