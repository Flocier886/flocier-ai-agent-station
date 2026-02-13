package com.flocier.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentEnumVO;
import com.flocier.domain.agent.model.vo.AiClientApiVO;
import com.flocier.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AiClientApiNode extends AbstractArmorySupport {
    @Resource
    private AiClientToolMcpNode aiClientToolMcpNode;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，API 构建节点 {}", JSON.toJSONString(armoryCommandEntity));
        //获取api数据信息
        List<AiClientApiVO> aiClientApiVOS=dynamicContext.getValue(dataName());
        if(aiClientApiVOS==null || aiClientApiVOS.isEmpty()){
            log.warn("没有需要被初始化的 ai client api");
            return router(armoryCommandEntity, dynamicContext);
        }
        for (AiClientApiVO aiClientApiVO:aiClientApiVOS){
            //构建openAiApi
            OpenAiApi openAiApi=OpenAiApi.builder()
                    .baseUrl(aiClientApiVO.getBaseUrl())
                    .apiKey(aiClientApiVO.getApiKey())
                    .completionsPath(aiClientApiVO.getCompletionsPath())
                    .embeddingsPath(aiClientApiVO.getEmbeddingsPath())
                    .build();
            //注册Bean对象
            registerBean(beanName(aiClientApiVO.getApiId()), OpenAiApi.class, openAiApi);
        }
        return router(armoryCommandEntity,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientToolMcpNode;
    }
    @Override
    protected String beanName(String id){
        return AiAgentEnumVO.AI_CLIENT_API.getBeanName(id);
    }
    @Override
    protected String dataName(){
        return AiAgentEnumVO.AI_CLIENT_API.getDataName();
    }
}
