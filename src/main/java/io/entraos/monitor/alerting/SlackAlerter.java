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

public class SlackAlerter implements Alerter {
    private static final Logger log = getLogger(SlackAlerter.class);
    public static final String ALERT_EMOJI = ":exclamation:";
    public static final String REVIVED_EMOJI = ":ok_hand:";
    private static final String SLACK_ALERTING_ENABLED_KEY = "slack_alerting_enabled";
    private static final String SLACK_TOKEN_KEY = "slack_token";
    private static final String SLACK_CHANNEL_KEY = "slack_channel";
    private final boolean alertingIsEnabled;
    private final String slackToken;
    private final Slack slack;
    private final String alertToChannel;
    private MethodsClient methodsClient = null;

    public SlackAlerter() {
        String slackAlertingEnabled = getProperty(SLACK_ALERTING_ENABLED_KEY);
        this.alertingIsEnabled = Boolean.valueOf(slackAlertingEnabled);
        this.slackToken = getProperty(SLACK_TOKEN_KEY);
        this.alertToChannel = getProperty(SLACK_CHANNEL_KEY);
        slack = Slack.getInstance();
        setupClient();
    }

    public SlackAlerter(Slack slack, boolean enableAlerting, String slackToken, String channel) {
        this.slack = slack;
        this.alertingIsEnabled = enableAlerting;
        this.slackToken = slackToken;
        this.alertToChannel = channel;
        setupClient();
    }

    void setupClient() {
        if (alertingIsEnabled) {
            methodsClient = slack.methods(slackToken);
        }
    }

    @Override
    public void notifyFailure(String message) {
        if (alertingIsEnabled) {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(alertToChannel)
                    .text(ALERT_EMOJI + message)
                    .build();

            try {
                ChatPostMessageResponse response = methodsClient.chatPostMessage(request);
                if (response != null && !response.isOk()) {
                    log.warn("Failed to send message: {} to channel: {}. Response: {}", message, alertToChannel, response);
                } else {
                    log.trace("Slack Response: {}", response);
                }
            } catch (IOException e) {
                log.trace("IOException when sending message: {} to channel {}. Reason: {}", message, alertToChannel, e.getMessage());
            } catch (SlackApiException e) {
                log.trace("SlackApiException when sending message: {} to channel {}. Reason: {}", message, alertToChannel, e.getMessage());
            }
        }
    }

    @Override
    public void notifyRevival(String message) {
        if (alertingIsEnabled) {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(alertToChannel)
                    .text(REVIVED_EMOJI + message)
                    .build();

            try {
                ChatPostMessageResponse response = methodsClient.chatPostMessage(request);
                if (response != null && !response.isOk()) {
                    log.warn("Failed to send message: {} to channel: {}. Response: {}", message, alertToChannel, response);
                } else {
                    log.trace("Slack Response: {}", response);
                }
            } catch (IOException e) {
                log.trace("IOException when sending message: {} to channel {}. Reason: {}", message, alertToChannel, e.getMessage());
            } catch (SlackApiException e) {
                log.trace("SlackApiException when sending message: {} to channel {}. Reason: {}", message, alertToChannel, e.getMessage());
            }
        }
    }

    @Override
    public boolean isAlertingEnabled() {
        return alertingIsEnabled;
    }

    public static void main(String[] args) throws InterruptedException {
        SlackAlerter alerter = new SlackAlerter();
        alerter.notifyFailure("Kaffekopp er tom.");
        Thread.sleep(10000);
        alerter.notifyRevival("Puh, ny kaffe på veg.");
    }
}
