package com.flocier.domain.agent.service.armory.business.data;

import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;

public interface ILoadDataStrategy {
    void loadData(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext);
}
