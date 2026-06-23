package com.portfolio.socialfeed;

import org.springframework.boot.SpringApplication;

public class TestSocialFeedApplication {

	public static void main(String[] args) {
		SpringApplication.from(SocialFeedApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
