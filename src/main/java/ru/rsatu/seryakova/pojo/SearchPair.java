package ru.rsatu.seryakova.pojo;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SearchPair {

    private String group;

    private String teacher;

    private List<String> grName;

    private String subgroup;

    private List<String> teachers;

    private List<String> discipline;

    private List<Integer> numberPar;

    private String todate;

    private List<String> room;

    private String monday;

    public String getMonday() {
        return monday;
    }

    public void setMonday(String monday) {
        this.monday = monday;
    }

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
        if (this.todate != null) {
            System.out.println(this.todate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            locDate = LocalDate.parse(this.todate, formatter);
        } else
        {
            //locDate = LocalDate.of(1900,1,1);
            locDate = null;
            return null;
        }

        return locDate;
    }
}
