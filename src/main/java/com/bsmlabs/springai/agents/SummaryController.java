package com.bsmlabs.springai.agents;

import com.bsmlabs.springai.models.PromptRequest;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SummaryController {

    private final BuiltInToolsAgent builtInToolsAgent;

    public SummaryController(BuiltInToolsAgent builtInToolsAgent) {
        this.builtInToolsAgent = builtInToolsAgent;
    }

    @GetMapping("/")
    public String home() {
        return "summary";
    }

    @PostMapping("/summarize")
    public String summarize(@RequestParam("url") String url, Model model) {

        try {

            String prompt = """
                    Open webpage: %s
                    
                    Read the webpage content once.
                    
                    Locate the primary content area.
                    
                    Prioritize content inside:
                    - <main>
                    - <article>
                    - documentation body
                    - blog content
                    
                    Provide:
                    1. Main purpose
                    2. Key highlights
                    3. Concise summary in bullet points
                    
                    
                    Ignore:
                    - navigation
                    - menus
                    - footer
                    - ads
                    - banners
                    - related links
                    
                    Extract only meaningful content.
                    Return only final answer.
                    Generate concise HTML summary using:
                    <h3>, <p>, <ul>, <li>
                    
                    Return only HTML.
                    """.formatted(url);
            String response = builtInToolsAgent.chat(
                    new PromptRequest(prompt),
                    new AgentCoreContext(new HttpHeaders())
            );
            model.addAttribute("summary", response);

        } catch (Exception e) {
            model.addAttribute("summary",
                    "Error occurred: " + e.getMessage());
        }

        return "summary";
    }
}
