package com.flocier.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.flocier.domain.agent.adapter.repository.IAgentRepository;
import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentClientFlowConfigVO;
import com.flocier.domain.agent.model.vo.AiAgentEnumVO;
import com.flocier.domain.agent.model.vo.AiAgentVO;
import com.flocier.domain.agent.service.IArmoryService;
import com.flocier.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArmoryService implements IArmoryService {
    @Resource
    private IAgentRepository repository;

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Override
    public List<AiAgentVO> acceptArmoryAllAvailableAgents() {
        List<AiAgentVO> aiAgentVOS = repository.queryAvailableAgents();
        for (AiAgentVO aiAgentVO : aiAgentVOS) {
            String agentId = aiAgentVO.getAgentId();
            acceptArmoryAgent(agentId);
        }
        return aiAgentVOS;
    }

    @Override
    public void acceptArmoryAgent(String agentId) {
        List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOS = repository.queryAiAgentClientsByAgentId(agentId);
        if (aiAgentClientFlowConfigVOS.isEmpty()) return;

        // 获取客户端集合
        List<String> commandIdList = aiAgentClientFlowConfigVOS.stream()
                .map(AiAgentClientFlowConfigVO::getClientId)
                .collect(Collectors.toList());

        try {
            StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                    defaultArmoryStrategyFactory.armoryStrategyHandler();

            armoryStrategyHandler.apply(
                    ArmoryCommandEntity.builder()
                            .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                            .commandIdList(commandIdList)
                            .build(),
                    new DefaultArmoryStrategyFactory.DynamicContext());
        } catch (Exception e) {
            throw new RuntimeException("装配智能体失败", e);
        }
    }

    @Override
    public List<AiAgentVO> queryAvailableAgents() {
        return repository.queryAvailableAgents();
    }

}
