package com.eduadmin.system.security;

import com.eduadmin.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthTokenStore tokenStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod hm = (HandlerMethod) handler;
        RequiresPerm ann = hm.getMethodAnnotation(RequiresPerm.class);
        if (ann == null) ann = hm.getBeanType().getAnnotation(RequiresPerm.class);
        if (ann == null) return true; // 无注解不拦截

        String required = ann.value();
        // 从 Authorization: Bearer <token> 解析权限
        String auth = request.getHeader("Authorization");
        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) token = auth.substring("Bearer ".length()).trim();
        // 使用副本避免修改存储中的原集合
        Set<String> perms = new java.util.HashSet<>(tokenStore.permsOf(token));
        // 兼容：如果前端传递了 X-Perms，则合并作为权限来源
        String xperms = request.getHeader("X-Perms");
        if (xperms != null && !xperms.trim().isEmpty()) {
            perms.addAll(Arrays.asList(xperms.split(",")));
        }
        boolean allowed = perms.contains("*") || perms.contains(required);
        if (!allowed) {
            writeForbidden(response, required);
            return false;
        }
        return true;
    }

    private void writeForbidden(HttpServletResponse response, String required) throws IOException {
        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(403, "后端权限不足：" + required));
    }
}