package com.flocier.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.flocier.domain.agent.adapter.repository.IAgentRepository;
import com.flocier.domain.agent.model.entity.ArmoryCommandEntity;
import com.flocier.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public abstract class AbstractArmorySupport extends AbstractMultiThreadStrategyRouter<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext,String> {
    private final Logger log = LoggerFactory.getLogger(AbstractArmorySupport.class);

    @Resource
    protected ApplicationContext applicationContext;
    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;
    @Resource
    protected IAgentRepository repository;
    @Override
    protected void multiThread(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 缺省的
    }
    protected synchronized <T> void registerBean(String beanName,Class<T> beanClass,T beanInstance){
        //获取注册Bean的spring工厂
        DefaultListableBeanFactory beanFactory=(DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        //注册bean对象
        BeanDefinitionBuilder definitionBuilder=BeanDefinitionBuilder.genericBeanDefinition(beanClass,()->beanInstance);
        BeanDefinition beanDefinition=definitionBuilder.getBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        //如果Bean对象存在，先移除
        if (beanFactory.containsBeanDefinition(beanName)){
            beanFactory.removeBeanDefinition(beanName);
        }
        //注册新的bean到spring容器
        beanFactory.registerBeanDefinition(beanName,beanDefinition);
        log.info("成功注册Bean: {}",beanName);
    }
    protected <T> T getBean(String beanName){
        return (T)applicationContext.getBean(beanName);
    }
    protected String beanName(String id){
        return null;
    }
    protected String dataName(){
        return null;
    }
}
