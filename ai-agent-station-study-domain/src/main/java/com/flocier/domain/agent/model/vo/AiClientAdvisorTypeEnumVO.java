package com.flocier.domain.agent.model.vo;

import com.flocier.domain.agent.service.armory.node.factory.element.RagAnswerAdvisor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientAdvisorTypeEnumVO {
    CHAT_MEMORY("ChatMemory","上下文记忆(内存模式)") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.ChatMemory chatMemory=aiClientAdvisorVO.getChatMemory();
            //这里设置的MessageWindowChatMemory是指会话的存储方法，这里并没有给PromptChatMemoryAdvisor设置conversionId，也就是说所以会话都只会存在一个List中而不是按桶进行Map存储，如果调用client时指明用哪个维度查询记忆也会无效
            //TODO这里可以考虑加conversionId
            return PromptChatMemoryAdvisor.builder(
                            MessageWindowChatMemory.builder()
                                    .maxMessages(chatMemory.getMaxMessages())
                                    .build()
                    )
                    .build();
        }
    },
    RAG_ANSWER("RagAnswer","知识库") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.RagAnswer ragAnswer=aiClientAdvisorVO.getRagAnswer();
            return new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
                    .topK(ragAnswer.getTopK())
                    .filterExpression(ragAnswer.getFilterExpression())
                    .build());
        }
    },

    ;

    private String code;
    private String info;

    private static final Map<String,AiClientAdvisorTypeEnumVO> CODE_MAP=new HashMap<>();
    static {
        for (AiClientAdvisorTypeEnumVO enumVO:values()){
            CODE_MAP.put(enumVO.getCode(),enumVO);
        }
    }

    public static AiClientAdvisorTypeEnumVO getByCode(String code){
        AiClientAdvisorTypeEnumVO enumVO = CODE_MAP.get(code);
        if(enumVO==null){
            throw new RuntimeException("err! advisorType " + code + " not exist!");
        }
        return enumVO;
    }
    /**
     * 直接在这里配置各种类型Advisor的实例化对象
     * */
    public abstract Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore);
}
