package ru.red.productservice.domain;

import lombok.Data;

@Data
public class Comment {
    private String authorEmail;
    private String content;
    private Boolean recommend;
}
