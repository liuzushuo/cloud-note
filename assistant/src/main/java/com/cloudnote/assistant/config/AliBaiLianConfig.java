package com.cloudnote.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云百炼的相关参数
 */
@ConfigurationProperties(prefix = "bailian")
@Component
@Data
public class AliBaiLianConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String agentKey;
    private String appId;
}
