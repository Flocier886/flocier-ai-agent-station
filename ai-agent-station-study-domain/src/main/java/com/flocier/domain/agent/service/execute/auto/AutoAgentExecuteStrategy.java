package com.flocier.domain.agent.service.execute.auto;

import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.service.execute.IExecuteStrategy;
import com.flocier.domain.agent.service.execute.auto.step.factory.DefaultAutoAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;

public class AutoAgentExecuteStrategy implements IExecuteStrategy {
    @Resource
    private DefaultAutoAgentExecuteStrategyFactory defaultAutoAgentExecuteStrategyFactory;
    @Override
    public void execute(ExecuteCommandEntity requestParameter) throws Exception {

    }
}
