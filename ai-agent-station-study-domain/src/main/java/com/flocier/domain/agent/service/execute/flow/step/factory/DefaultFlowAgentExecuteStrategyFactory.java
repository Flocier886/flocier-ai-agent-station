package com.flocier.domain.agent.service.execute.flow.step.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentClientFlowConfigVO;
import com.flocier.domain.agent.service.execute.flow.step.RootNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DefaultFlowAgentExecuteStrategyFactory {
    private final RootNode flowRootNode;

    public DefaultFlowAgentExecuteStrategyFactory(RootNode rootNode) {
        this.flowRootNode = rootNode;
    }

    public StrategyHandler<ExecuteCommandEntity ,DefaultFlowAgentExecuteStrategyFactory.DynamicContext,String>armoryStrategyHandler(){
        return this.flowRootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext{

        private int step = 1;

        private int maxStep = 4;

        private String currentTask;

        private StringBuilder executionHistory;

        boolean isCompleted=false;

        private Map<String, AiAgentClientFlowConfigVO>aiAgentClientFlowConfigVOMap;

        private Map<String, Object>dataObjects=new HashMap<>();

        public <T> void setValue(String key, T data){
            dataObjects.put(key,data);
        }

        public <T> T getValue(String key){
            return (T)dataObjects.get(key);
        }
    }
}
