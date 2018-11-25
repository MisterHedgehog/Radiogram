package com.MMGS.radiogram.classes;

public class Friend {
    private String name;
    private String id;

    public Friend(String name, String id){
    this.name = name;
    this.id = id;
    }

    public Friend(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
