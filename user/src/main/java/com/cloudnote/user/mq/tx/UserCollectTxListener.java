package com.cloudnote.user.mq.tx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.user.api.domain.User;
import com.cloudnote.user.service.ILoginService;
import com.cloudnote.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQTransactionListener(txProducerGroup = "CN_USER_COLLECT_TX_PRODUCER_GROUP")
public class UserCollectTxListener implements RocketMQLocalTransactionListener {
    @Autowired
    private ILoginService loginService;
    @Autowired
    private IUserService userService;
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        String jsonString = new String((byte[]) message.getPayload());
        JSONObject params = JSONUtil.parseObj(jsonString);
        Integer userId = params.getInt("userId");
        String password = params.getStr("password");
        String email = params.getStr("email");
        try {
            boolean result = loginService.registWithTx(userId, password, email);
            if (!result){
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String jsonString = new String((byte[]) message.getPayload());
        JSONObject params = JSONUtil.parseObj(jsonString);
        Integer userId = params.getInt("userId");
        User user = userService.getById(userId);
        // 如果用户存在，则表示事务已经成功
        if (user != null) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
