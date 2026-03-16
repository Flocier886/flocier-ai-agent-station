package com.flocier.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentEnumVO;
import com.flocier.domain.agent.model.vo.AiClientSystemPromptVO;
import com.flocier.domain.agent.model.vo.AiClientVO;
import com.flocier.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiClientNode extends AbstractArmorySupport {
    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，客户端{}", JSON.toJSONString(armoryCommandEntity));

        List<AiClientVO> aiClientVOS = dynamicContext.getValue(dataName());
        if(aiClientVOS==null || aiClientVOS.isEmpty()){
            return router(armoryCommandEntity, dynamicContext);
        }
        //提取出prompt信息
        Map<String, AiClientSystemPromptVO>aiClientAdvisorVOMap=dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName());
        for (AiClientVO aiClientVO:aiClientVOS){
            //拼接预设话术
            StringBuilder defaultSystem=new StringBuilder("Ai 智能体 \r\n");
            List<String>promptIdList=aiClientVO.getPromptIdList();
            for (String promptId:promptIdList){
                AiClientSystemPromptVO aiClientSystemPromptVO=aiClientAdvisorVOMap.get(promptId);
                defaultSystem.append(aiClientSystemPromptVO.getPromptContent());
            }
            //对话模型
            OpenAiChatModel chatModel=getBean(aiClientVO.getModelBeanName());
            //MCP服务(目前默认mcp服务都配置在model中)
            //log.info("clientVO: {}",JSON.toJSONString(aiClientVO));
            List<McpSyncClient> mcpSyncClients = new ArrayList<>();
            List<String> mcpBeanNameList = aiClientVO.getMcpBeanNameList();
            for (String mcpBeanName : mcpBeanNameList) {
                mcpSyncClients.add(getBean(mcpBeanName));
            }
            McpSyncClient[] mcpSyncClientArray=mcpSyncClients.toArray(new McpSyncClient[]{});
            //advisor服务
            List<Advisor> advisors = new ArrayList<>();
            List<String> advisorBeanNameList = aiClientVO.getAdvisorBeanNameList();
            for (String advisorBeanName : advisorBeanNameList) {
                advisors.add(getBean(advisorBeanName));
            }
            Advisor[] advisorArray=advisors.toArray(new Advisor[]{});
            //构建client
            ChatClient client=ChatClient.builder(chatModel)
                    .defaultSystem(defaultSystem.toString())
                    .defaultToolCallbacks(SyncMcpToolCallbackProvider.builder()
                            .mcpClients(mcpSyncClientArray)
                            .build())
                    .defaultAdvisors(advisorArray)
                    .build();
            //注册bean对象
            registerBean(beanName(aiClientVO.getClientId()),ChatClient.class,client);
        }
        return router(armoryCommandEntity,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
    @Override
    protected String beanName(String id){
        return AiAgentEnumVO.AI_CLIENT.getBeanName(id);
    }
    @Override
    protected String dataName(){
        return AiAgentEnumVO.AI_CLIENT.getDataName();
    }
}
