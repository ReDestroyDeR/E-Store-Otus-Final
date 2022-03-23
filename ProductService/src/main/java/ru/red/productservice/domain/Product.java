package ru.red.productservice.domain;

import lombok.Data;

import java.util.List;

@Data
public class Product {
    private String name;
    private Integer inStock;
    private List<Comment> commentList;

    public boolean addComment(Comment comment) {
        return this.commentList.add(comment);
    }

    public boolean removeComment(Comment comment) {
        return this.commentList.remove(comment);
    }
}
