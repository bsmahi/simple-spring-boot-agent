package com.bsmlabs.springai.agents;

import com.bsmlabs.springai.models.ChatRequest;
import com.bsmlabs.springai.models.ChatResponse;
import org.springaicommunity.agentcore.memory.longterm.AgentCoreMemory;
import org.springaicommunity.agentcore.memory.shorttem.AgentCoreShortTermMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ShortTermMemoryController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final AgentCoreMemory agentCoreMemory;

    private static final String CONVERSATION_ID = UUID.randomUUID().toString();

    public ShortTermMemoryController(ChatClient.Builder chatClientBuilder,
                                     ChatMemory chatMemory,
                                     AgentCoreMemory agentCoreMemory,
                                     AgentCoreShortTermMemoryRepository shortTermMemoryRepository) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        this.agentCoreMemory = agentCoreMemory;

        // shortTermMemoryRepository.deleteByConversationId(CONVERSATION_ID);
    }

    @PostMapping("/api/short")
    public ChatResponse shortTermChat(@RequestBody ChatRequest chatRequest) {
        String response = chatClient.prompt()
                .user(chatRequest.message())
                .advisors(agentCoreMemory.advisors)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
                .call()
                .content();

        return new ChatResponse(response);
    }


    @GetMapping("/api/history")
    public List<Message> getHistory() {
        return chatMemory.get(CONVERSATION_ID);
    }

    @DeleteMapping("/api/history")
    public void clearHistory() {
        chatMemory.clear(CONVERSATION_ID);
    }

}
