package com.cloudnote.assistant.service.impl;

import com.aliyun.broadscope.bailian.sdk.AccessTokenClient;
import com.aliyun.broadscope.bailian.sdk.ApplicationClient;
import com.aliyun.broadscope.bailian.sdk.BaiLianSdkException;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsRequest;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsResponse;
import com.cloudnote.assistant.api.dto.MessageDto;
import com.cloudnote.assistant.config.AliBaiLianConfig;
import com.cloudnote.assistant.service.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChatServiceImpl implements IChatService {
    @Autowired
    private AliBaiLianConfig config;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * 向阿里云百炼发送消息
     *
     * @param messageDto
     * @param principal
     */
    @Override
    public void senMessage(MessageDto messageDto, Principal principal) {
        // 获取用户id
        String userId = principal.getName();
        // 向阿里云百炼发送消息
        AccessTokenClient accessTokenClient = new AccessTokenClient(config.getAccessKeyId(), config.getAccessKeySecret(), config.getAgentKey());
        String token = accessTokenClient.getToken();
        ApplicationClient client = ApplicationClient.builder()
                .token(token)
                .build();

        CompletionsRequest request = new CompletionsRequest()
                .setAppId(config.getAppId())
                .setPrompt(messageDto.getContent());

        CountDownLatch latch = new CountDownLatch(1);
        Flux<CompletionsResponse> response = client.streamCompletions(request);
        response.subscribe(
                data -> {
                    if (data.isSuccess()) {
                        messagingTemplate.convertAndSendToUser(userId,"/queue/chatMessage/receive", data.getData().getText());
                    } else {
                        log.info("failed to create completion, requestId: {}, code: {}, message: {}\n",
                                data.getRequestId(), data.getCode(), data.getMessage());
                    }
                },
                err -> {
                    log.info("failed to create completion, err: {}\n", ExceptionUtils.getStackTrace(err));
                    latch.countDown();
                },
                () -> {
                    log.info("create completion completely");
                    latch.countDown();
                }
        );

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new BaiLianSdkException(e);
        }
    }
}
