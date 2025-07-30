package com.weyland.prototype.bishopprototype;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.weyland.prototype.bishopprototype",
		"com.weyland.starter.synthetichumancorestarter"
})
public class BishopPrototypeApplication {
	public static void main(String[] args) {
		SpringApplication.run(BishopPrototypeApplication.class, args);
	}
}