package com.flocier.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentEnumVO;
import com.flocier.domain.agent.model.vo.AiClientModelVO;
import com.flocier.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiClientModelNode extends AbstractArmorySupport{
    @Resource
    private AiClientAdvisorNode aiClientAdvisorNode;
    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Mode 对话模型{}", JSON.toJSONString(armoryCommandEntity));
        List<AiClientModelVO> aiClientModelVOS=dynamicContext.getValue(dataName());
        if(aiClientModelVOS==null || aiClientModelVOS.isEmpty()){
            log.warn("没有需要被初始化的 ai client model");
            return router(armoryCommandEntity,dynamicContext);
        }
        for (AiClientModelVO aiClientModelVO:aiClientModelVOS){
            //获取相关联的api和mcp的Bean对象
            OpenAiApi openAiApi=getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(aiClientModelVO.getApiId()));
            if (openAiApi==null){
                throw new RuntimeException("mode 2 api is null");
            }
            List<McpSyncClient> mcpSyncClients=new ArrayList<>();
            for (String mcpId:aiClientModelVO.getToolMcpIds()){
                McpSyncClient mcpAsyncClient=getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(mcpId));
                mcpSyncClients.add(mcpAsyncClient);
            }
            //实例化对话模型
            OpenAiChatModel chatModel=OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(
                            OpenAiChatOptions.builder()
                                    .model(aiClientModelVO.getModelName())
                                    .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                                    .build()
                    )
                    .build();
            //注册Bean对象
            registerBean(beanName(aiClientModelVO.getModelId()),OpenAiChatModel.class,chatModel);
        }
        return router(armoryCommandEntity,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientAdvisorNode;
    }
    @Override
    protected String beanName(String id){
        return AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(id);
    }
    @Override
    protected String dataName(){
        return AiAgentEnumVO.AI_CLIENT_MODEL.getDataName();
    }
}
