package com.devtaco.distribute.config;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * elastic job 에서 사용할 registry Center 를 만드는 configuration
 */
@Configuration
@ConfigurationProperties(prefix = "application.job-reg-center")
@Getter
@Setter
public class JobRegConfig {
  private String serverLists;
  private String namespace;

  /**
   * ElasticJob 에서 사용할 CoordinatorRegistryCenter 를 생성한다. zookeeper 서버 리스트와,
   * job 들이 등록될 namespace( root dir name on ZooKeeper 라 보면 됨)이 있어야 한다.
   * Bean 으로 선언해서 필요한 놈들이 가져다 쓰도록 하자.
   * 
   * @return
   */
  @Bean
  public CoordinatorRegistryCenter elasticJobRegistryCenter() {
    ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(serverLists, namespace);
    CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
    result.init();
    return result;
  }
}
