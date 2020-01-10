package ru.rsatu.seryakova.POJO;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ObjSearch {

    @SerializedName("nameG")//выбранная для поиска группа
    private String group;

    @SerializedName("teacher") //выбраный для поиска день
    private String teacher;

    @SerializedName("name")//выбранная для поиска группа
    private List<String> grName;

    @SerializedName("subgroup") //выбранная для поиска дата
    private String subgroup;

    @SerializedName("Teachers") //выбраный для поиска день
    private List<String> teachers;

    @SerializedName("discipline") //выбраный для поиска день
    private List<String> discipline;

    @SerializedName("numberPair") //выбранный для поиска № пары
    private List<Integer> numberPar;

    @SerializedName("date") //выбранная для поиска дата
    private String todate;

    @SerializedName("room") //выбранная для поиска аудитория
    private List<String> room;

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(String subgroup) {
        this.subgroup = subgroup;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getGrName() {
        return grName;
    }

    public void setGrName(List<String> grName) {
        this.grName = grName;
    }

    public List<String> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<String> teacher) {
        this.teachers = teacher;
    }

    public List<String> getDiscipline() {
        return discipline;
    }

    public void setDiscipline(List<String> discipline) {
        this.discipline = discipline;
    }

    public List<Integer> getNumberPar() {
        return numberPar;
    }

    public void setNumberPar(List<Integer> numberPar) {
        this.numberPar = numberPar;
    }

    public List<String> getRoom() {
        return room;
    }

    public void setRoom(List<String> room) {
        this.room = room;
    }

    public LocalDate getDate() {
        LocalDate locDate;
        if (todate != null) {
            System.out.println(todate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            locDate = LocalDate.parse(todate, formatter);
        } else
        {
            //locDate = LocalDate.of(1900,1,1);
            locDate = null;
        }

        return locDate;
    }
}