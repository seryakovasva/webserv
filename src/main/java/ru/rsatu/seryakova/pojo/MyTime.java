package ru.rsatu.seryakova.pojo;

public class MyTime {
    private Integer week;
    private String monday;
    private String sunday;

    public MyTime(){}
    public MyTime(Integer week, String monday, String sunday) {
        this.week = week;
        this.monday = monday;
        this.sunday = sunday;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public String getMonday() {
        return monday;
    }

    public void setMonday(String monday) {
        this.monday = monday;
    }

    public String getSunday() {
        return sunday;
    }

    public void setSunday(String sunday) {
        this.sunday = sunday;
    }
}
