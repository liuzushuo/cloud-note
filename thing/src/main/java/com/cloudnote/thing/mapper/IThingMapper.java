package com.cloudnote.thing.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudnote.thing.api.domain.Thing;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IThingMapper extends BaseMapper<Thing> {

}
