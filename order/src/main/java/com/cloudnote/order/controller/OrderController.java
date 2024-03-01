package com.cloudnote.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.order.api.domain.PayRecord;
import com.cloudnote.order.api.dto.AddOrderDto;
import com.cloudnote.order.api.dto.PayRecordDto;
import com.cloudnote.order.config.AlipayConfig;
import com.cloudnote.order.config.UserHolder;
import com.cloudnote.order.constants.StatusCode;
import com.cloudnote.order.service.IOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_KEY;

@Slf4j
@Controller
public class OrderController {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @Autowired
    private IOrdersService ordersService;

    /**
     * 生成支付二维码
     *
     * @param addOrderDto
     * @return
     */

    @ResponseBody
    @PostMapping("/generatepaycode")
    public R generatePayCode(AddOrderDto addOrderDto) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.生成支付二维码
        PayRecordDto payRecordDto = ordersService.createOrder(userId, addOrderDto);
        return payRecordDto!=null?R.ok(payRecordDto):R.fail("生成二维码失败");
    }

    /**
     * 扫码下单
     *
     * @param payNo
     * @param httpResponse
     * @throws IOException
     */
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException, AlipayApiException {
        // 1.判断支付记录是否存在
        PayRecord payRecord = ordersService.getPayRecordByPayno(payNo);
        if (payRecord == null) {
            throw new RuntimeException("支付记录不存在");
        }
        // 2.判断支付记录是否已经支付
        if (payRecord.getStatus().equals(StatusCode.PAY_STATUS_PAID)) {
            throw new RuntimeException("已支付，无需重复支付");
        }
        // 3.请求支付宝进行支付
        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.URL,
                APP_ID,
                APP_PRIVATE_KEY,
                AlipayConfig.FORMAT,
                AlipayConfig.CHARSET,
                ALIPAY_PUBLIC_KEY,
                AlipayConfig.SIGNTYPE
        );
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        //异步接收地址，仅支持http/https，公网可访问
//        request.setNotifyUrl("https://7a88-112-54-84-218.ngrok-free.app/paynotify");

        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", payNo);
        //支付金额，最小值0.01元
        bizContent.put("total_amount", payRecord.getTotalPrice());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", payRecord.getOrderName());

        /******可选参数******/
        //手机网站支付默认传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
        String timeExpire = LocalDateTime.now().plus(30, ChronoUnit.MINUTES).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 设置订单过期时间
        bizContent.put("time_expire", timeExpire);

        request.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            log.info("调用成功");
            String form = response.getBody();
            httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
            httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
            httpResponse.getWriter().flush();
        } else {
            log.info("调用失败");
        }
    }

    /**
     * 查询支付结果
     * @param payNo
     * @return
     * @throws IOException
     */
    @GetMapping("/payresult")
    @ResponseBody
    public R payresult(String payNo,HttpServletRequest request) throws IOException {
        // 获取token
        String token = request.getHeader("authorization");
        token = token.split(" ")[1];
        // 获取用户信息
        String tokenKey = LOGIN_USER_KEY + token;
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询支付结果
        PayRecordDto payRecordDto = ordersService.queryPayResult(userId, payNo,tokenKey);
        return R.ok(payRecordDto, payRecordDto.getStatus().equals(StatusCode.PAY_STATUS_PAID) ? "支付成功" : "未找到支付记录");
    }
}
