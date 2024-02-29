package com.cloudnote.note.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    @Value("${es.host}")
    private String host;
    @Value("${es.port}")
    private Integer port;

    @Bean
    public RestHighLevelClient client(){
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host,port,"http")));
    }
}
