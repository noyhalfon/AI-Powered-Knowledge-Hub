package com.knowledgehub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
    
    @Autowired
    @NonNull
    private SecurityInterceptor securityInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
       
        registry.addInterceptor(securityInterceptor).addPathPatterns("/api/**");
    }
}
