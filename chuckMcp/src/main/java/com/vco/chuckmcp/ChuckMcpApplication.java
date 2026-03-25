package com.vco.chuckmcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChuckMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChuckMcpApplication.class, args);
    }

    @Bean
    ToolCallbackProvider chuckNorrisTools(ChuckNorrisFactsTool chuckNorrisFactsTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(chuckNorrisFactsTool)
                .build();
    }

}
