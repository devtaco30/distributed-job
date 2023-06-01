package com.devtaco.distribute;

import java.net.UnknownHostException;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.devtaco.distribute.zk.EmbedZookeeperServer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) throws UnknownHostException {
		
    // embeded zookeeper server 구동 
    // port 는 알아서...
		// TODO : local 에서만 사용한다. 
		EmbedZookeeperServer.start(1111); // 
		
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    SpringApplication.run(SpringBootServletInitializer.class, args);

  }

}
