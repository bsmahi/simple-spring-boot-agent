package com.bsmlabs.springai.agents;

import com.bsmlabs.springai.models.PromptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springaicommunity.agentcore.artifacts.ArtifactStore;
import org.springaicommunity.agentcore.artifacts.GeneratedFile;
import org.springaicommunity.agentcore.artifacts.SessionConstants;
import org.springaicommunity.agentcore.browser.BrowserArtifacts;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springaicommunity.agentcore.context.AgentCoreHeaders;
import org.springaicommunity.agentcore.memory.longterm.AgentCoreMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class BuiltInToolsAgent {

    private static final Logger logger = LoggerFactory.getLogger(BuiltInToolsAgent.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final AgentCoreMemory agentCoreMemory;
    private final ArtifactStore<GeneratedFile> artifactStore;

    @Value("${app.output-dir}")
    private String outputDir;

    public BuiltInToolsAgent(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            AgentCoreMemory agentCoreMemory,
            @Qualifier("browserToolCallbackProvider") ToolCallbackProvider browserTools,
            @Qualifier("browserArtifactStore") ArtifactStore<GeneratedFile> artifactStore) {
        this.chatMemory = chatMemory;
        this.agentCoreMemory = agentCoreMemory;
        this.artifactStore = artifactStore;

        // Build ChatClient WITH browser tools integrated
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(browserTools)
                .build();
    }

    @AgentCoreInvocation
    public String chat(PromptRequest request, AgentCoreContext context) throws IOException {
        String sessionId = "session-" + UUID.randomUUID();

        logger.info("Chat initiated - Session: {}, Prompt: {}", sessionId, request.prompt());
        String response = chatClient.prompt()
                .user(request.prompt())
                .stream()
                .content()
                .contextWrite(ctx -> ctx.put(SessionConstants.SESSION_ID_KEY, sessionId))
                .collectList()
                .map(chunks -> String.join("", chunks))
                .block();
        logger.info("Response: {}", response);

        // Retrieve screenshots from artifact store (using default category)
        List<GeneratedFile> screenshots = artifactStore.retrieve(sessionId);
        if (screenshots == null || screenshots.isEmpty()) {
            logger.warn("No screenshots captured");
        }

        // Save screenshots to disk
        Path dir = Path.of(outputDir);
        Files.createDirectories(dir);

        for (int i = 0; i < Objects.requireNonNull(screenshots).size(); i++) {
            storeArtifactsGenerated(screenshots, dir, i, logger);
        }

        return "Done";

    }

    public static void storeArtifactsGenerated(List<GeneratedFile> screenshots, Path dir, int i, Logger logger) throws IOException {
        var screenshot = screenshots.get(i);
        String filename = screenshots.size() == 1 ? "screenshot.png" : "screenshot-" + i + ".png";
        Path file = dir.resolve(filename);
        Files.write(file, screenshot.data());

        String screenshotUrl = BrowserArtifacts.url(screenshot).orElse("unknown");
        logger.info("Screenshot saved: {} ({} bytes) from {}", file.toAbsolutePath(), screenshot.data().length,
                screenshotUrl);
    }
}