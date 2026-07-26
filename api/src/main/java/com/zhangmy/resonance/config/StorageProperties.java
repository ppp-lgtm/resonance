package com.zhangmy.resonance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "resonance.storage")
public class StorageProperties {
    /** 上传文件本地存储目录 */
    private String localDir = "./uploads/";
    /** 对外访问前缀 */
    private String publicUrlPrefix = "http://localhost:8080/files";
}
