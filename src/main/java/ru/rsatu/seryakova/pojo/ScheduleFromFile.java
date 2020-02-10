package ru.rsatu.seryakova.pojo;

public class ScheduleFromFile {
    private String group;
    private String dayWeek;
    private String number;
    private String week;
    private String typeWeek;
    private String subGroup;
    private String type;
    private String dis;
    private String teacher;
    private String room;
    private Integer row;

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row + 1;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String groups) {
        this.group = groups;
    }

    public String getDayWeek() {
        return dayWeek;
    }

    public void setDayWeek(String dayWeek) {
        this.dayWeek = dayWeek;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getTypeWeek() {
        return typeWeek;
    }

    public void setTypeWeek(String typeWeek) {
        this.typeWeek = typeWeek;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDis() {
        return dis;
    }

    public void setDis(String dis) {
        this.dis = dis;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teachers) {
        this.teacher = teachers;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
