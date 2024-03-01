package com.cloudnote.order.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.order.api.domain.PayRecord;
import com.cloudnote.order.mapper.IPayRecordMapper;
import com.cloudnote.order.service.IPayRecordService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author 42456
 * @since 2023-12-15
 */
@Service
public class PayRecordServiceImpl extends ServiceImpl<IPayRecordMapper, PayRecord> implements IPayRecordService {

}
