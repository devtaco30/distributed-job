/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devtaco.distribute.zk;

import java.io.File;
import java.io.IOException;

import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embed ZooKeeper.
 * 
 * <p>
 *     Only used for examples
 * </p>
 */
public final class EmbedZookeeperServer {
  private final static Logger logger = LoggerFactory.getLogger(EmbedZookeeperServer.class);
    
  private static TestingServer testingServer;
  
  /**
   * Embed ZooKeeper. local 에서의 테스트용. staging/production 등 서버 환경에서는 필요 없다.
   * 
   * @param port ZooKeeper port
   */
  public static void start(final int port) {
    try {
      logger.info("Starting Testing ZOOKEEPER... port: {}", port);

      testingServer = new TestingServer(port, new File(String.format("target/test_zk_data/%s/", System.nanoTime())));

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          Thread.sleep(1000L);
          testingServer.close();
        } 
        catch (final InterruptedException | IOException ignore) {
        }
      }));

    // CHECKSTYLE:OFF
    } catch (final Exception ex) {
    // CHECKSTYLE:ON
      ex.printStackTrace();
    } 
  }
}
