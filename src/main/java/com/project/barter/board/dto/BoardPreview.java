package com.project.barter.board.dto;

import com.project.barter.board.Board;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class BoardPreview {

    private Long id;

    private String title;

    private LocalDateTime writeTime;

    private String loginId;

    public static BoardPreview byBoard(Board board) {
        return BoardPreview.builder()
                .id(board.getId())
                .title(board.getTitle())
                .writeTime(board.getCreateDate())
                .loginId(board.getUser().getLoginId())
                .build();
    }
}
