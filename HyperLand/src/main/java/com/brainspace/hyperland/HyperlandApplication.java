package com.brainspace.hyperland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;


//@EnableResourceServer
@SpringBootApplication
public class HyperlandApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(HyperlandApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(HyperlandApplication.class);
	}
}
