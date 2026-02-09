package com.flocier.domain.agent.service.execute;

import com.flocier.domain.agent.model.entity.ExecuteCommandEntity;

public interface IExecuteStrategy {

    void execute(ExecuteCommandEntity requestParameter)throws Exception;
}
