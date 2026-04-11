package com.flocier.domain.agent.service.execute.flow.step;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.flocier.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentClientFlowConfigVO;
import com.flocier.domain.agent.model.vo.AiClientTypeEnumVO;
import com.flocier.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Step1McpToolsAnalysisNode extends AbstractExecuteSupport{
    @Resource
    private Step2PlanningNode step2PlanningNode;
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n--- 步骤1: MCP工具能力分析（仅分析阶段，不执行用户请求） ---");
        //获取配置信息
        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO=dynamicContext.getAiAgentClientFlowConfigVOMap().get(AiClientTypeEnumVO.TOOL_MCP_CLIENT.getCode());
        //获取相应客户端
        ChatClient mcpToolsChatClient=getChatClientByClientId(aiAgentClientFlowConfigVO.getClientId());
        //配置相应prompt
        String mcpAnalysisPrompt = String.format(
                """
                        # MCP工具能力分析任务
                        
                        ## 重要说明
                        **注意：本阶段仅进行MCP工具能力分析，不执行用户的实际请求。**
                        本阶段生成的所有工具参数示例，均为【通用格式演示】，并非真实业务参数！
                      
                        ## 用户请求
                        %s
                      
                        ## 分析要求
                        请基于上述实际的MCP工具信息，针对用户请求进行详细的工具能力分析（仅分析，不执行）：
                      
                        ### 1. 工具匹配分析
                        - 分析每个可用工具的核心功能和适用场景
                        - 评估哪些工具能够满足用户请求的具体需求
                        - 标注每个工具的匹配度（高/中/低）
                        
                        ### 2. 工具使用指南
                        - 提供每个相关工具的具体调用方式
                        - 说明必需的参数和可选参数的含义（仅说明参数作用，不提供真实业务值）
                        - 给出参数的【示例值和格式要求】，⚠️ 重要标记：
                          1.  所有示例参数（如索引名、字段名、服务名等）均为随机生成，非真实业务数据
                          2.  示例仅用于演示参数格式，不可直接用于后续执行
                          3.  真实业务参数必须从后续执行端的系统prompt约束与RAG知识库中获取
                          4.  示例参数标注格式：【非真实业务参数】XXX（如【非真实业务参数】index: "demo-log-2026.04.06"）
                        
                        ### 3. 执行策略建议
                        - 推荐最优的工具组合方案
                        - 建议工具的调用顺序和依赖关系
                        - 提供备选方案和降级策略
                        
                        ### 4. 注意事项
                        - 标注工具的使用限制和约束条件
                        - 提醒可能的错误情况和处理方式
                        - 给出性能优化建议
                        
                        ### 5. 分析总结
                        - 明确说明这是分析阶段，不要执行任何实际操作
                        - 总结工具能力评估结果
                        - 再次强调：本阶段所有参数示例均为【非真实业务参数】，后续执行需从系统prompt和RAG获取真实值
                        - 为后续执行阶段提供建议
                        
                        请确保分析结果准确、详细、可操作，所有示例参数必须按要求标注【非真实业务参数】，严禁遗漏标记。""",
                dynamicContext.getCurrentTask()
        );
        String mcpToolsAnalysis=mcpToolsChatClient.prompt()
                .user(mcpAnalysisPrompt)
                .call()
                .content();
        log.info("MCP工具分析结果（仅分析，未执行实际操作）: {}", mcpToolsAnalysis);
        //保存结果至上下文
        dynamicContext.setValue("mcpToolsAnalysis", mcpToolsAnalysis);
        //发生SSE结果
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                dynamicContext.getStep(),
                "analysis_tools",
                mcpToolsAnalysis,
                executeCommandEntity.getSessionId());
        sendSseResult(dynamicContext, result);
        //更新步骤
        dynamicContext.setStep(dynamicContext.getStep()+1);

        return router(executeCommandEntity,dynamicContext);

    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return step2PlanningNode;
    }
}
