package com.example.lee.footprints.model;

// 검색시 리스트뷰 한칸에 들어갈 요소
public class Tag {
    private String id;
    private int num;

    public Tag(String name, int count){
        id = name;
        num = count;
    }

    public Tag(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}