package com.flocier.domain.agent.service.execute.fixed;

import com.alibaba.fastjson.JSON;
import com.flocier.domain.agent.adapter.repository.IAgentRepository;
import com.flocier.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentClientFlowConfigVO;
import com.flocier.domain.agent.model.vo.AiAgentEnumVO;
import com.flocier.domain.agent.service.IExecuteStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service("fixedAgentExecuteStrategy")
public class FixedAgentExecuteStrategy implements IExecuteStrategy {
    @Resource
    private IAgentRepository repository;
    @Resource
    private ApplicationContext applicationContext;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Override
    public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception {
        //获取client列表
        List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOS=repository.queryAiClientsByAgentId(requestParameter.getAiAgentId());
        //循环执行客户端
        String content="";
        for (AiAgentClientFlowConfigVO flowConfigVO:aiAgentClientFlowConfigVOS){
            ChatClient chatClient=getChatClientByClientId(flowConfigVO.getClientId());

            content=chatClient.prompt(requestParameter.getMessage()+"，"+content)
                    .system(s->s.param("current_date", LocalDate.now().toString()))
                    .advisors(a->a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY,requestParameter.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,100))
                    .call().content();

            log.info("智能体对话进行，客户端ID {}", requestParameter.getAiAgentId());
        }
        log.info("智能体对话请求，结果 {} {}", requestParameter.getAiAgentId(), content);
        // 发送最终结果通知（确保 content 不为空）
        if (content != null && !content.trim().isEmpty()) {
            sendFinalResult(emitter, content, requestParameter.getSessionId());
        }
        // 发送完成标识
        sendCompleteResult(emitter, requestParameter.getSessionId());

    }

    private ChatClient getChatClientByClientId(String clientId) {
        return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
    }

    private <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }
    /**
     * 发送最终结果到流式输出
     */
    private void sendFinalResult(ResponseBodyEmitter emitter, String content, String sessionId) {
        try {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSummaryResult(content, sessionId);
            String sseData = "data: " + JSON.toJSONString(result) + "\n\n";
            emitter.send(sseData);
            log.info("✅ 已发送最终结果");
        } catch (Exception e) {
            log.error("发送最终结果失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发送完成标识到流式输出
     */
    private void sendCompleteResult(ResponseBodyEmitter emitter, String sessionId) {
        try {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createCompleteResult(sessionId);
            String sseData = "data: " + JSON.toJSONString(result) + "\n\n";
            emitter.send(sseData);
            log.info("✅ 已发送完成标识");
        } catch (Exception e) {
            log.error("发送完成标识失败：{}", e.getMessage(), e);
        }
    }

}
