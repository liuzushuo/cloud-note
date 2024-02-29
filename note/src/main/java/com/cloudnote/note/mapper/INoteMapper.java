package com.cloudnote.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudnote.note.api.domain.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface INoteMapper extends BaseMapper<Note> {

}
