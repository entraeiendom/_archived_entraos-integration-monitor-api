package io.entraos.monitor.alerting;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.slf4j.Logger;

import java.io.IOException;

import static no.cantara.config.ServiceConfig.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

public class SlackAlerter {
    private static final Logger log = getLogger(SlackAlerter.class);
    private static final String SLACK_ALERTING_ENABLED = "slack_alerting_enabled";
    private static final String SLACK_TOKEN = "slack_token";
    public static final String ALERT_EMOJI = ":exclamation:";
    public static final String REVIVED_EMOJI = ":ok_hand:";
    private final Boolean alertingIsEnabled;
    private final String slackToken;
    private final Slack slack;
    private MethodsClient methodsClient = null;

    public SlackAlerter() {
        String slackAlertingEnabled = getProperty(SLACK_ALERTING_ENABLED);
        this.alertingIsEnabled = Boolean.valueOf(slackAlertingEnabled);
        this.slackToken = getProperty(SLACK_TOKEN);
        slack = Slack.getInstance();
        setupClient();
    }

    public SlackAlerter(Slack slack, boolean enableAlerting, String slackToken) {
        this.slack = slack;
        this.alertingIsEnabled = enableAlerting;
        this.slackToken = slackToken;
        setupClient();
    }

    void setupClient() {
        if (alertingIsEnabled) {
            methodsClient = slack.methods(slackToken);
        }
    }

    public void notifyFailure(String channel, String message) {
        if (alertingIsEnabled) {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channel)
                    .text(ALERT_EMOJI + message)
                    .build();

            try {
                ChatPostMessageResponse response = methodsClient.chatPostMessage(request);
                log.info("Slack Response: {}", response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SlackApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyRevival(String channel, String message) {
        if (alertingIsEnabled) {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channel)
                    .text(REVIVED_EMOJI + message)
                    .build();

            try {
                ChatPostMessageResponse response = methodsClient.chatPostMessage(request);
                if (response != null && !response.isOk()) {
                    log.warn("Failed to send message: {} to channel: {}. Response: {}", message, channel, response);
                } else {
                    log.trace("Slack Response: {}", response);
                }
            } catch (IOException e) {
                log.trace("IOException when sending message: {} to channel {}. Reason: {}", message, channel, e.getMessage());
            } catch (SlackApiException e) {
                log.trace("SlackApiException when sending message: {} to channel {}. Reason: {}", message, channel, e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SlackAlerter alerter = new SlackAlerter();
        alerter.notifyFailure("#entraos-playground", "Kaffekopp er tom.");
        Thread.sleep(10000);
        alerter.notifyRevival("#entraos-playground", "Puh, ny kaffe på veg.");
    }
}
