package com.bsmlabs.springai.agents;

import com.bsmlabs.springai.models.PromptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.artifacts.SessionConstants;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springaicommunity.agentcore.memory.longterm.AgentCoreMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class BuiltInToolsAgent {

    private static final Logger logger = LoggerFactory.getLogger(BuiltInToolsAgent.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final AgentCoreMemory agentCoreMemory;

    public BuiltInToolsAgent(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            AgentCoreMemory agentCoreMemory,
            @Qualifier("browserToolCallbackProvider")
            ToolCallbackProvider browserTools) {

        this.chatMemory = chatMemory;
        this.agentCoreMemory = agentCoreMemory;

        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(browserTools)
                .build();
    }

    public String chat(PromptRequest request,
                       AgentCoreContext context) throws IOException {

        String sessionId = "session-" + UUID.randomUUID();
        logger.info(
                "Chat initiated - Session: {}, Prompt: {}",
                sessionId,
                request.prompt()
        );

        String response = chatClient.prompt()
                .user(request.prompt())
                .advisors(advisorSpec ->
                        advisorSpec.param(
                                SessionConstants.SESSION_ID_KEY,
                                sessionId
                        )
                )
                .call()
                .content();

        logger.info("Response: {}", response);

        return response;
    }
}