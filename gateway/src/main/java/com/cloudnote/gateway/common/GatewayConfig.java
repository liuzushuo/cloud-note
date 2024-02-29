package com.cloudnote.gateway.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Autowired
    private IgnoreWhiteProperties ignoreWhiteProperties;
    @Bean
    public TokenFilter tokenFilter(){
        return new TokenFilter(ignoreWhiteProperties);
    }
}
