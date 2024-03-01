package com.cloudnote.order.api.dto;

import com.cloudnote.order.api.domain.PayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * @author Mr.M
 * @version 1.0
 * @description 支付记录dto
 * @date 2022/10/4 11:30
 */
@Data
@ToString
public class PayRecordDto extends PayRecord {

    //二维码
    private String qrcode;

}
