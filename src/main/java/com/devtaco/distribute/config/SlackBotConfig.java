package com.devtaco.distribute.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.Getter;

@Getter
@Configuration
@ConfigurationProperties(prefix = "slack")
public class SlackBotConfig {

  @Autowired
  private Environment env;

  private String channel;
  private String token;

}
