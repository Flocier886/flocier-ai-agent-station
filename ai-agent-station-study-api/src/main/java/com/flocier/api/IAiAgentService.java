package com.flocier.api;

import com.flocier.api.dto.AutoAgentRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IAiAgentService {
    ResponseBodyEmitter autoAgent(AutoAgentRequestDTO request, HttpServletResponse response);
}
