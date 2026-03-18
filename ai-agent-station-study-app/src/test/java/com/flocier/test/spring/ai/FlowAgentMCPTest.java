package com.flocier.test.spring.ai;

import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowAgentMCPTest {
    @Test
    public void test() {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl("https://apis.itedus.cn")
                        .apiKey("sk-NoLyjkzzk1pkxFaQD870B7F4D28d4759931b7a507382A11b")
                        .completionsPath("v1/chat/completions")
                        .embeddingsPath("v1/embeddings")
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4.1")
                        .toolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClientElasticsearch()).getToolCallbacks())
                        .build())
                .build();

        ChatResponse call = chatModel.call(Prompt.builder().messages(new UserMessage("有哪些工具可以使用")).build());
        log.info("测试结果:{}", JSON.toJSONString(call.getResult()));
    }

    public McpSyncClient Arthas_Mcp(){
        HttpClientStreamableHttpTransport streamableHttpTransport=HttpClientStreamableHttpTransport.builder("http://192.168.100.128:8563")
                .httpRequestCustomizer((builder, method, endpoint, body, context) -> builder.header("Authorization","Bearer lOT6BBVcp7koaBmXN5wEecz1h7WF7S8DhXkTyAVy3IQmHGKlVN04e595mlsxnUrg"))
                .endpoint("/mcp")
                .build();
        McpSyncClient mcpSyncClient=McpClient.sync(streamableHttpTransport).requestTimeout(Duration.ofMinutes(360)).build();
        var init = mcpSyncClient.initialize();
        log.info("Arthas MCP StreamableHttp Initialized: {}", init);
        return mcpSyncClient;
    }

    /**
     * https://github.com/awesimon/elasticsearch-mcp
     * https://www.npmjs.com/package/@awesome-ai/elasticsearch-mcp
     * npm i @awesome-ai/elasticsearch-mcp
     */
    public McpSyncClient stdioMcpClientElasticsearch() {
        Map<String, String> env = new HashMap<>();
        env.put("ES_HOST", "http://192.168.100.128:9200");
        env.put("ES_API_KEY", "");

        var stdioParams = ServerParameters.builder("D:/node.js/npx.cmd")
                .args("-y", "@awesome-ai/elasticsearch-mcp")
                .env(env)
                .build();

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams, McpJsonMapper.getDefault()))
                .requestTimeout(Duration.ofSeconds(100)).build();

        var init = mcpClient.initialize();

        System.out.println("Stdio MCP Initialized: " + init);

        return mcpClient;

    }

    public McpSyncClient stdioMcpClient_Grafana() {
        Map<String, String> env = new HashMap<>();
        env.put("GRAFANA_URL", "http://192.168.100.128:3000");
        env.put("GRAFANA_API_KEY", "glsa_XlQ49CFV7ZEdppcAhaL7IXIFyudOrS1p_b048562c");

        var stdioParams = ServerParameters.builder("docker")
                .args("run",
                        "--rm",
                        "-i",
                        "-e",
                        "GRAFANA_URL",
                        "-e",
                        "GRAFANA_API_KEY",
                        "mcp/grafana",
                        "-t",
                        "stdio")
                .env(env)
                .build();

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams,McpJsonMapper.getDefault()))
                .requestTimeout(Duration.ofSeconds(100)).build();

        var init = mcpClient.initialize();
        log.info("Stdio MCP Initialized: {}", init);

        return mcpClient;

    }

}
