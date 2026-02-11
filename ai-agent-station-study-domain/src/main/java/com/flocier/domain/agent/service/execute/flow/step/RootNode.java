package com.flocier.domain.agent.service.execute.flow.step;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentClientFlowConfigVO;
import com.flocier.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service("flowRootNode")
public class RootNode extends AbstractExecuteSupport{
    @Resource
    private Step1McpToolsAnalysisNode step1McpToolsAnalysisNode;
    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("=== 流程执行开始 ====");
        log.info("用户输入: {}", requestParameter.getMessage());
        log.info("最大执行步数: {}", requestParameter.getMaxStep());
        log.info("会话ID: {}", requestParameter.getSessionId());

        Map<String, AiAgentClientFlowConfigVO>aiAgentClientFlowConfigVOMap=repository.queryAiAgentClientFlowConfig(requestParameter.getAiAgentId());
        //当前任务
        dynamicContext.setCurrentTask(requestParameter.getMessage());
        //客户端对话组
        dynamicContext.setAiAgentClientFlowConfigVOMap(aiAgentClientFlowConfigVOMap);
        //最大步数
        dynamicContext.setMaxStep(requestParameter.getMaxStep());
        //上下文信息
        dynamicContext.setExecutionHistory(new StringBuilder());

        return router(requestParameter,dynamicContext);

    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return step1McpToolsAnalysisNode;
    }
}
