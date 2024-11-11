package com.oreo.finalproject_5re5_be.audio.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AudioFileInfo {
    private String name;
    private String size;      //크기 (용량)
    private Long length;    // 초 단위 길이
    private String extension; //확장자
}
