package com.devtaco.distribute.util;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * OkHttpClient 에 attach 할 RateLimit 적용 Interceptor. <p>
 * bucket 을 체크하는 건 아니다. <p>
 */
@Slf4j
public class RateLimitInterceptor implements Interceptor {

  private static RateLimitInterceptor instance = new RateLimitInterceptor();
  
  public static RateLimitInterceptor getInstance() {
    return instance;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {

    Response response = chain.proceed(chain.request());

    // 429 code ==> too many request
    if (!response.isSuccessful() && response.code() == 429 ) {
      // wait & retry
      try {
        log.info("{} is over rate limit,  wait and retry... {}", response.request().url().toString());
        response.close();
        Thread.sleep( 1000L ); // 1초가 지나면, RateLimit Bucket 의 token 도 초기화 된다.
      } catch ( InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      response = chain.proceed(chain.request());
    }
    return response;
  }

}
