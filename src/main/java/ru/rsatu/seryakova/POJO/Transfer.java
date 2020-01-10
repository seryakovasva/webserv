package ru.rsatu.seryakova.POJO;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transfer {

    @SerializedName("id_el") //выбраный для поиска день
    private Long id_el;

    @SerializedName("teacher") //выбраный для поиска день
    private String teacher;

    @SerializedName("numberPair") //выбранный для поиска № пары
    private Integer numberPar;

    @SerializedName("date") //выбранная для поиска дата
    private String todate;

    @SerializedName("room") //выбранная для поиска аудитория
    private String room;

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public Long getId_el() {
        return id_el;
    }

    public void setId_el(Long id_el) {
        id_el = id_el;
    }


    public Integer getNumberPar() {
        return numberPar;
    }


    public void setNumberPar(Integer numberPar) {
        this.numberPar = numberPar;
    }

//    public void setTodate(String todate) {
//        this.todate = todate;
//    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
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
