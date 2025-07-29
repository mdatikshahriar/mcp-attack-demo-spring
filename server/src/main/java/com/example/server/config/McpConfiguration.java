package com.example.server.config;

import com.example.server.service.UpdateSignalService;
import com.example.server.service.WeatherService;
import com.example.server.tools.MathTools;
import com.example.server.tools.McpCommandLineRunner;
import io.modelcontextprotocol.server.McpSyncServer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {

    @Bean
    public ToolCallbackProvider allTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService, new MathTools())
                .build();
    }

    @Bean
    public CommandLineRunner commandRunner(McpSyncServer mcpSyncServer,
            UpdateSignalService updateSignalService) {
        return new McpCommandLineRunner(mcpSyncServer, updateSignalService);
    }
}
