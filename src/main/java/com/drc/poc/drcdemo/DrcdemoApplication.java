package com.drc.poc.drcdemo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
//http://localhost:8080/swagger-ui/index.html#
@OpenAPIDefinition(
		info = @Info(
				title = "DCR Demo",
				version = "0.1"
		)
)
public class DrcdemoApplication{
	public static void main(String[] args) {
		SpringApplication.run(DrcdemoApplication.class, args);
	}

}
