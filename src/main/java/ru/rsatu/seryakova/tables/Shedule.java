package ru.rsatu.seryakova.tables;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@Entity
@Table(name = "Shedule")//Название таблицы в БД
public class Shedule{//} implements Serializable {

    public Shedule() {}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="idElSeq")
    //@Column(name = "id_el")
    private Long id_el;

//    @Column(name = "namegroup")
    private String nameGroup;

//    @Column(name = "numberpair")
    private Integer numberPair;

//    @Column(name = "subGroup")
    private String subgroup;

//    @Column(name = "dayweek")
    private String dayWeek;

//    @Column(name = "room")
    private String room;

//    @Column(name = "discipline")
    private String discipline;

//    @Column(name = "teacher")
    private String teacher;

//    @Column(name = "typepair")
    private String typepair;

//    @Column(name = "day")
    private String date;

//    @Column(name = "info")
    private String info;

    public Long getId_el() {
        return id_el;
    }

    public void setId_el(Long id_el) {
        this.id_el = id_el;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }

    public Integer getNumberPair() {
        return numberPair;
    }

    public void setNumberPair(Integer numberPair) {
        this.numberPair = numberPair;
    }

    public String getSubGroup() {
        return subgroup;
    }


    public void setSubGroup(String subgroup) {
        this.subgroup = subgroup;
    }

    public String getDayWeek() {
        return dayWeek;
    }

    public void setDayWeek(String dayWeek) {
        this.dayWeek = dayWeek;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTypepair() {
        return typepair;
    }

    public void setTypepair(String typepair) {
        this.typepair = typepair;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
