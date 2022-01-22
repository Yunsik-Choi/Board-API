package com.project.barter.board;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter @Setter
@Entity
public class Board {

    @GeneratedValue
    @Id
    private Long id;

    private String title;

    private String content;

}