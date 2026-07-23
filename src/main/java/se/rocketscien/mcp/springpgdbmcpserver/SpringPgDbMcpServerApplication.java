package se.rocketscien.mcp.springpgdbmcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import se.rocketscien.mcp.springpgdbmcpserver.tools.DatabaseMcpServerTools;

@ConfigurationPropertiesScan
@SpringBootApplication
class SpringPgDbMcpServerApplication {

    @Bean
    ToolCallbackProvider tools(DatabaseMcpServerTools databaseMcpServerTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(databaseMcpServerTools)
                .build();
    }

    static void main(String[] args) {
        SpringApplication.run(SpringPgDbMcpServerApplication.class, args);
    }
}
