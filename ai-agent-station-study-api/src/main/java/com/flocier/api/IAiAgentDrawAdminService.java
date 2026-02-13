package com.flocier.api;

import com.flocier.api.dto.AiAgentDrawConfigRequestDTO;
import com.flocier.api.dto.AiAgentDrawConfigResponseDTO;
import com.flocier.api.response.Response;

public interface IAiAgentDrawAdminService {
    /**
     * 保存拖拉拽流程图配置
     *
     * @param request 配置请求参数
     * @return 保存结果
     */
    Response<String> saveDrawConfig(AiAgentDrawConfigRequestDTO request);

    /**
     * 获取拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 配置数据
     */
    Response<AiAgentDrawConfigResponseDTO> getDrawConfig(String configId);

    /**
     * 删除拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    Response<String> deleteDrawConfig(String configId);

}
