package com.zpedroo.voltzcash.category;

import java.util.List;

public class Category {

    private String title;
    private Integer size;
    private List<CategoryItem> items;

    public Category(String title, Integer size, List<CategoryItem> items) {
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public Integer getSize() {
        return size;
    }

    public List<CategoryItem> getItems() {
        return items;
    }
}