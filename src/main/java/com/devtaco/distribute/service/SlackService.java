package com.devtaco.distribute.service;

import static com.devtaco.distribute.util.HttpRequestUtils.requestBy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.stereotype.Service;

import com.devtaco.distribute.config.SlackBotConfig;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

@Slf4j
@Service
public class SlackService {

  private static final String BEARER_STRING = "Bearer";
  private static final String SLACK_CHAT_BASE_URL = "https://slack.com/api/chat.postMessage?";
  private static final String SLACK_BOT_PROVIDER = "slack";

  private static String slackBotToken;
  private static String slackChannelId;
  private static ObjectMapper mapper;

  public SlackService(SlackBotConfig slackConfig) {
    SlackService.slackBotToken = slackConfig.getToken();
    SlackService.slackChannelId = slackConfig.getChannel();
    SlackService.mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  }

  @Getter
  @Setter
  @Builder
  public static class SlackMsgContent {
  }

  public void sendErrorMessage(String provider, String title, String message) {
    String slackMsg = provider + ":" + title + ":" + message;
    sendAlert(slackMsg);
  }

  public void sendErrorMessage(String provider, String title, String message, String exceptionMessage) {
    String slackMsg = provider + ":" + title + ":" + message + ":" + exceptionMessage;
    sendAlert(slackMsg);
  }

  public void sendInfoMessage(String provider, String message) {
    String slackMsg = provider + ":" + message;
    sendAlert(slackMsg);

  }

  public boolean sendAlert(String message) {
    String urlStr = SLACK_CHAT_BASE_URL;
    urlStr += "channel=" + slackChannelId; // channel id
    try {
      urlStr += "&blocks=" + URLEncoder.encode(message, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Slack Message Encode fail", e);
    }

    Request req = new Request.Builder()
        .get()
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .addHeader("Authorization", getBeaerToken())
        .url(urlStr)
        .build();

    String slackSendResult = requestBy(req, SLACK_BOT_PROVIDER);
    JsonNode resultNode = createReulstNode(slackSendResult);
    if (null == resultNode) {
      return false;
    }

    return resultNode.get("ok").asBoolean();
  }

  private static String getBeaerToken() {
    return BEARER_STRING + " " + slackBotToken;
  }

  public JsonNode createReulstNode(String jsonString) {
    try {
      return mapper.readTree(jsonString);
    } catch (JsonProcessingException e) {
      log.error(" get JsonNode from resultString occur Exception ==> ", e);
      return null;
    }
  }

}
