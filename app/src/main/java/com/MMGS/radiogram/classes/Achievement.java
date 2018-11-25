package com.MMGS.radiogram.classes;

import java.util.ArrayList;

public class Achievement {
    private String name;
    private String title;
    private int type;
    private int value;
    private int maxValue;
    private String text;
    public static final int CLOSED_ACHIEVEMENT = 0;
    public static final int OPENED_ACHIEVEMENT = 1;

    public Achievement(String nameOfAchievement, int typeOfAchievement, int maxValueOfAchievement) {
        this.name = nameOfAchievement;
        this.type = typeOfAchievement;
        this.maxValue = maxValueOfAchievement;
        this.value = 0;
        switch (name){
            case "alpha" : this.text = "Благодарность за участие в Альфа-тестировании."; this.title = "Пионер-разведчик"; break;
            case "beta" : this.text = "Благодарность за участие в Бета-тестировании."; this.title = "Пионер-испытатель";break;
            case "background_edit" : this.text = "Выдаётся за изменение фона в профиле."; this.title = "Редактор";break;
            case "messages_x5" : this.text = "Выдаётся за написание пяти сообщений в общий чат."; this.title = "Меня услышат!";break;
            case "messages_x25" : this.text = "Выдаётся за написание 25 сообщений в общий чат."; this.title = "Писать не строить";break;
            case "messages_x100" : this.text = "Выдаётся за написание ста сообщений в общий чат."; this.title = "Захват чата";break;
        }
    }

    public Achievement() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
