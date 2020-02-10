package ru.rsatu.seryakova.pojo;

public class Search {
    private String nameGroup;
    private String room;
    private String teacher;
    private String discipline;

    public Search(String NameGroup, String room, String teacher, String discipline){
        this.nameGroup = NameGroup;
        this.room = room;
        this.teacher = teacher;
        this.discipline = discipline;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public String getRoom() {
        return room;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getDiscipline() {
        return discipline;
    }
}
