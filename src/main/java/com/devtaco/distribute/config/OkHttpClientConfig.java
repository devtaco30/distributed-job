package com.devtaco.distribute.config;

import static okhttp3.internal.Util.threadFactory;

import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * 외부로 API Call 이 있을 경우 사용한다. <p>
 * OkHttpClient Pool 을 만들어 사용한다. <p>
 * client pool size : 3
 */
public class OkHttpClientConfig {

  private OkHttpClient okHttpClient;
  private OkHttpClient pingOkHttpClient;

  private static OkHttpClientConfig INSTANCE = new OkHttpClientConfig();

  public static OkHttpClientConfig getInstance() {
    return INSTANCE;
  }

  private OkHttpClientConfig() {
    this.pingOkHttpClient = getPingOkHttpClient();
    this.okHttpClient = getOkHttpClientWithPing();
  }

  public OkHttpClient getOkHttpClient() {
    return this.okHttpClient;
  }

  public OkHttpClient getPingOkHttpClient() {
    return this.pingOkHttpClient;
  }

  private OkHttpClient createOkHttpClient() {
    ThreadPoolExecutor dispatcherExecutor = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 60,
        TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(), threadFactory("OK-Dispatcher", false));

    Dispatcher commonDispatcher = new Dispatcher(dispatcherExecutor);
    commonDispatcher.setMaxRequests(128);
    commonDispatcher.setMaxRequestsPerHost(16);

    ConnectionPool commonConnectionPool = new ConnectionPool(128, 5, TimeUnit.MINUTES);
    return new OkHttpClient.Builder()
        .connectionPool(commonConnectionPool)
        .connectionSpecs(
            Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .dispatcher(commonDispatcher)
        .build();
  }

  private OkHttpClient getOkHttpClientWithPing() {
    if (this.pingOkHttpClient == null) {
      pingOkHttpClient = createOkHttpClient();
    }
    return pingOkHttpClient.newBuilder()
        .pingInterval(10, TimeUnit.SECONDS)
        .build();
  }
}
