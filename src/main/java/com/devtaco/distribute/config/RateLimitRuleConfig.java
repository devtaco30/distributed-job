package com.devtaco.distribute.config;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import com.devtaco.distribute.util.HttpRequestUtils;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * REST 요청 시 limit rule 을 설정해둔다. 과도한 요청 방지 <p>
 * 1sec에 10번 , 1nanoSec 에 1번으로 제한을 해두었다.
 */
@Slf4j
public class RateLimitRuleConfig {

  private LocalBucketBuilder bucketBuilder;
  private ConcurrentHashMap<String, Bucket> limiterMap;

  private Bandwidth limitTenPerSec;
  private Bandwidth limitOnePerMilliSec;

  private static RateLimitRuleConfig instance = new RateLimitRuleConfig();

  public static RateLimitRuleConfig getInstance() {
    return instance;
  }

  private RateLimitRuleConfig() {
    initialize();
  }

  /**
   * {@link HttpRequestUtils } 에서 사용한다
   * <p>
   * bean 들이 올라오는 도중에 이 method 를 사용할 경우 이 config의 객체 초기화에 실패한다.
   * <p>
   * 없으면 null check 를 하고 만들도록 initialize 한다.
   */
  private void initialize() {

    if (null == this.bucketBuilder) {
      this.bucketBuilder = new LocalBucketBuilder();
    }

    if (null == this.limiterMap) {
      this.limiterMap = new ConcurrentHashMap<>();
    }

    if (null == this.limitTenPerSec) {
      this.limitTenPerSec = Bandwidth.simple(10, Duration.ofSeconds(1));
    }

    if (null == this.limitOnePerMilliSec) {
      this.limitOnePerMilliSec = Bandwidth.simple(1, Duration.ofMillis(100));
    }
  }

  public void addLimiter(String apiProvider) {
    if (null == this.bucketBuilder) {
      log.info("bucketBuilder not yet created --> create new bucketBuilder");
      initialize();
    }

    Bucket limiter = null;

    limiter = this.bucketBuilder.addLimit(limitTenPerSec).addLimit(limitOnePerMilliSec).build();
    limiter.getAvailableTokens();

    this.limiterMap.put(apiProvider, limiter);
  }

  /**
   * bucket의 token 을 사용할 수 있는지 없는지를 return
   * 만약, bucket 에 등록된 provider 가 아니면 등록부터 한다.
   */
  public boolean checkRateLimitBy(String provider) {

    if (!limiterMap.containsKey(provider)) {
      addLimiter(provider);
    }

    Bucket limiter = this.limiterMap.get(provider);
    return limiter.tryConsume(1);

  }

}
