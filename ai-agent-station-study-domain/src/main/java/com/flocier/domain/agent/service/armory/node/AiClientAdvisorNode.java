package com.flocier.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentEnumVO;
import com.flocier.domain.agent.model.vo.AiClientAdvisorTypeEnumVO;
import com.flocier.domain.agent.model.vo.AiClientAdvisorVO;
import com.flocier.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiClientAdvisorNode extends AbstractArmorySupport {
    @Resource
    private VectorStore vectorStore;
    @Resource
    private AiClientNode aiClientNode;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Advisor 顾问角色{}", JSON.toJSONString(armoryCommandEntity));
        List<AiClientAdvisorVO> aiClientAdvisorVOS =dynamicContext.getValue(dataName());
        if(aiClientAdvisorVOS==null || aiClientAdvisorVOS.isEmpty()){
            log.warn("没有需要被初始化的 ai client advisor");
            return router(armoryCommandEntity, dynamicContext);
        }
        for (AiClientAdvisorVO aiClientAdvisorVO:aiClientAdvisorVOS){
            //创建advisor对象
            Advisor advisor=createAdvisor(aiClientAdvisorVO);
            //注册Bean对象
            registerBean(beanName(aiClientAdvisorVO.getAdvisorId()), Advisor.class,advisor);
        }
        return router(armoryCommandEntity,dynamicContext);
    }

    private Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO) {
        String advisorType = aiClientAdvisorVO.getAdvisorType();
        AiClientAdvisorTypeEnumVO enumVO=AiClientAdvisorTypeEnumVO.getByCode(advisorType);
        return enumVO.createAdvisor(aiClientAdvisorVO,vectorStore);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }
    @Override
    protected String beanName(String id){
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(id);
    }
    @Override
    protected String dataName(){
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName();
    }
}
