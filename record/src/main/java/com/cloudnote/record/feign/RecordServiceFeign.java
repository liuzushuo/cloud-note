package com.cloudnote.record.feign;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.record.api.dto.RecordDto;
import com.cloudnote.record.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RecordServiceFeign {
    @Autowired
    private IRecordService recordService;


    /**
     * 添加一条操作记录
     *
     * @param recordVO
     * @param isRollBack // 是否回滚
     * @return
     */
    @PostMapping("/add")
    public R addRecord(@RequestBody RecordDto recordVO, @RequestParam("isRollBack") Boolean isRollBack) {
        // 添加操作记录
        boolean result = recordService.addRecord(recordVO, isRollBack);
        return result? R.ok() : R.fail();
    }


    /**
     * 删除一条操作记录
     *
     * @param userId
     * @param recordId
     * @param type
     * @param isRollBack
     * @return
     */
    @DeleteMapping("/delete")
    public R removeRecord(@RequestParam("userId") Integer userId, @RequestParam("recordId") Long recordId, @RequestParam("type") Integer type, @RequestParam("isRollBack") Boolean isRollBack) {
        // 删除操作记录
        boolean result = recordService.removeRecord(userId, recordId, type, isRollBack);
        return result? R.ok() : R.fail();
    }
}
