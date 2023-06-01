package com.devtaco.distribute.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import lombok.Getter;

/**
 * Thread Pool 을 만들어두고 꺼내 쓴다. <p>
 * 단일 Thread 로 하고 싶지만, 일정 시간에 여러 값 산출을 진행해야하기에 Multi Thread 를 사용한다. <p>
 */
public class ThreadPoolConfig {

  @Getter
  ThreadPoolTaskScheduler scheduledThreadPool;

  public static ThreadPoolConfig getInstance() {
    return ThreadPoolConfig.INSTANCE;
  }

  private static final ThreadPoolConfig INSTANCE = new ThreadPoolConfig();

  private ThreadPoolConfig() {
    this.scheduledThreadPool = createScheduler();
  }

  public ThreadPoolTaskScheduler createScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("application-sheduler-");
    scheduler.initialize();
    scheduler.setPoolSize(3);

    return scheduler;
  }
}
