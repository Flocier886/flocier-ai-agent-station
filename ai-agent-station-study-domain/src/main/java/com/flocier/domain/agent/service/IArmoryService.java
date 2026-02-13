package com.flocier.domain.agent.service;

import com.flocier.domain.agent.model.vo.AiAgentVO;

import java.util.List;

public interface IArmoryService {
    List<AiAgentVO> acceptArmoryAllAvailableAgents();

    void acceptArmoryAgent(String agentId);

    List<AiAgentVO> queryAvailableAgents();

}
