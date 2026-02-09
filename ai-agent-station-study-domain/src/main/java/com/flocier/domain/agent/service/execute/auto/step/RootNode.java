package com.flocier.domain.agent.service.execute.auto.step;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentClientFlowConfigVO;
import com.flocier.domain.agent.service.execute.auto.step.factory.DefaultAutoAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service("executeRootNode")
public class RootNode extends AbstractExecuteSupport{
    @Resource
    private Step1AnalyzerNode step1AnalyzerNode;
    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("=== 动态多轮执行测试开始 ====");
        log.info("用户输入: {}", requestParameter.getMessage());
        log.info("最大执行步数: {}", requestParameter.getMaxStep());
        log.info("会话ID: {}", requestParameter.getSessionId());
        Map<String, AiAgentClientFlowConfigVO>aiAgentClientFlowConfigVOMap=repository.queryAiAgentClientFlowConfig(requestParameter.getAiAgentId());
        //设置客户端对话组
        dynamicContext.setAiAgentClientFlowConfigVOMap(aiAgentClientFlowConfigVOMap);
        //设置最大步数
        dynamicContext.setMaxStep(requestParameter.getMaxStep());
        //设置上下文信息
        dynamicContext.setExecutionHistory(new StringBuilder());
        //设置当前任务信息
        dynamicContext.setCurrentTask(requestParameter.getMessage());

        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return step1AnalyzerNode;
    }
}
