package com.cloudnote.collect.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudnote.collect.api.domain.Collect;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICollectMapper extends BaseMapper<Collect> {

}
