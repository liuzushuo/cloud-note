package com.cloudnote.note.api.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_note_body")
public class NoteBody {
    @JsonFormat(shape =JsonFormat.Shape.STRING)
    private Long noteId;
    private String body;
}
