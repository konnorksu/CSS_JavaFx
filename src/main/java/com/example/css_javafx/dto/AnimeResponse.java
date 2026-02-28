package com.example.css_javafx.dto;

import java.util.List;

public class AnimeResponse {
    public List<AnimeDto> data;
    public Pagination pagination;

    public static class Pagination {
        public boolean has_next_page;
        public int last_visible_page;
        public Items items;

        public static class Items {
            public int count;
            public int total;
            public int per_page;
        }
    }
}