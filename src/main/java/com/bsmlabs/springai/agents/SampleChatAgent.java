package com.bsmlabs.springai.agents;

import com.bsmlabs.springai.models.PromptRequest;
import com.bsmlabs.springai.tools.MathematicalTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springaicommunity.agentcore.context.AgentCoreHeaders;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SampleChatAgent {

    private static final Logger logger = LoggerFactory.getLogger(SampleChatAgent.class);

    private final ChatClient chatClient;

    public SampleChatAgent(ChatClient.Builder chatClient){
        this.chatClient = chatClient
                .defaultTools(new MathematicalTools())
                .build();
    }

    /**
     * <code>@AgentCoreInvocation</code> marks a method as the agent invocation handler for the AgentCore runtime.
     * You can annotate only one method per application with this annotation.
     * Multiple @AgentCoreInvocation methods found. Only one is allowed in MVP.
     */
    //@AgentCoreInvocation
    public String agentCoreHandler(PromptRequest promptRequest,
                                   AgentCoreContext agentCoreContext){
        String sessionId = agentCoreContext.getHeader(AgentCoreHeaders.SESSION_ID);

        logger.info(agentCoreContext.getHeader(AgentCoreHeaders.SESSION_ID));

        return chatClient.prompt()
                .user(promptRequest.prompt())
                .call()
                .content();
    }

    // Using Streaming
//    @AgentCoreInvocation
//    public Flux<String> streamingChat(PromptRequest request) {
//        return chatClient.prompt()
//                .user(request.prompt())
//                .stream()
//                .content();
//    }

}
