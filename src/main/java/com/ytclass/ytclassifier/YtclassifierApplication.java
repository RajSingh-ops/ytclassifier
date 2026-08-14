package com.ytclass.ytclassifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class YtclassifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(YtclassifierApplication.class, args);
	}

}
