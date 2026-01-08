package com.knowledgehub.config;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String method = request.getMethod();
        String role = request.getHeader("role");

        if (method.equals("DELETE")) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only ADMIN can perform this action");
                return false; 
            }
        }
        if (method.equals("POST") && request.getRequestURI().contains("/api/document/uploadDocument")) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only ADMIN can perform this action");
                return false; 
            }
        }


        return true;
    }
}