package com.flocier.domain.agent.service.dispatch;

import com.flocier.domain.agent.adapter.repository.IAgentRepository;
import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;
import com.flocier.domain.agent.model.vo.AiAgentVO;
import com.flocier.domain.agent.service.IAgentDispatchService;
import com.flocier.domain.agent.service.execute.IExecuteStrategy;
import com.flocier.types.exception.BizException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class AgentDispatchService implements IAgentDispatchService {
    @Resource
    private Map<String, IExecuteStrategy> executeStrategyMap;

    @Resource
    private IAgentRepository repository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void dispatch(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception {
        AiAgentVO aiAgentVO=repository.queryAiAgentByAgentId(requestParameter.getAiAgentId());

        String strategy= aiAgentVO.getStrategy();
        IExecuteStrategy executeStrategy=executeStrategyMap.get(strategy);
        if (null == executeStrategy) {
            throw new BizException("不存在的执行策略类型 strategy:" + strategy);
        }
        // 异步执行AutoAgent
        threadPoolExecutor.execute(() -> {
            try {
                executeStrategy.execute(requestParameter, emitter);
            } catch (Exception e) {
                log.error("AutoAgent执行异常：{}", e.getMessage(), e);
                try {
                    emitter.send("执行异常：" + e.getMessage());
                } catch (Exception ex) {
                    log.error("发送异常信息失败：{}", ex.getMessage(), ex);
                }
            } finally {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("完成流式输出失败：{}", e.getMessage(), e);
                }
            }
        });

    }
}
