package com.flocier.test.spring.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AiClientTokenTest {
    @Autowired
    private RestClient.Builder builder;

    @Test
    public void test(){
        ChatClient chatClient= ChatClient.builder(OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl("https://apis.itedus.cn")
                        .apiKey("sk-IO21QRWX86W3EcL9E770E2104f0145598fEb43Ef5bB13b09")
                        .completionsPath("v1/chat/completions")
                        .embeddingsPath("v1/embeddings")
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4.1")
                        //.toolCallbacks(new SyncMcpToolCallbackProvider(sseMcpClient()).getToolCallbacks())
                        .build())
                .build()).build();
        String prompt="搜索小傅哥技术博客有哪些项目";
        ChatClient.CallResponseSpec callSpec = chatClient
                .prompt()
                .user(prompt)
                .call();
    }

    public McpSyncClient sseMcpClient() {
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport.builder("http://appbuilder.baidu.com/v2/ai_search/mcp/")
                .sseEndpoint("sse?api_key=bce-v3/ALTAK-D2i7sNgfb9YTf76efrnae/58afda5efb370bd6a9ba7035d19d5bc95000f367")
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(360)).build();
        var init_sse = mcpSyncClient.initialize();
        log.info("Tool SSE MCP Initialized {}", init_sse);

        return mcpSyncClient;
    }
}
