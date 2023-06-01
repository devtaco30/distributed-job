package com.devtaco.distribute.util;

import java.io.IOException;
import java.net.SocketTimeoutException;

import com.devtaco.distribute.config.OkHttpClientConfig;
import com.devtaco.distribute.config.RateLimitRuleConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class HttpRequestUtils {

  private static final OkHttpClient client = OkHttpClientConfig.getInstance()
      .getOkHttpClient()
      .newBuilder()
      .addInterceptor(RateLimitInterceptor.getInstance()).build();
  private static final RateLimitRuleConfig rateLimit = RateLimitRuleConfig.getInstance();

  public static String requestBy(Request request, String provider) {

    checkReqLimit(provider);

    String result = null;
    Response response = null;
    try {
      response = client.newCall(request).execute();
      if (response.isSuccessful() || response.code() == 304) { // response code 200..299 / 304 (caching)
        result = response.body().string();
      } else {
        // 실패할 경우 일단 로그만 남긴다.
        log.info("fail to get Response. Reqeuset isSuccessful ? : {} to {}", response.isSuccessful(),
            provider);
      }
    } catch (SocketTimeoutException e) {
      log.error("okHttpClient reqeust fail to SoketTimeout Exception ==> " + request.body(), e);
    } catch (IOException e) {
      log.error("okHttpClient request to exachange occur Exception ==> " + request.body(), e);
    } finally {
      // resource 해제 필수 -> memory leak 의 원인이 될 수 있는 부분이다.
      if (null != response) {
        response.close();
      }
    }

    return result;

  }

  /**
   * RateLimitRule 에 따라 limiter에 해당 provider 를 등록 및 limit 체크한다.
   */
  public static void checkReqLimit(String provider) {

    while (!rateLimit.checkRateLimitBy(provider)) {

      try {
        log.info("{} has limited so wait 100 millis", provider);
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.error("While Checking rateLimit, try thread sleep and occur exception", e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
