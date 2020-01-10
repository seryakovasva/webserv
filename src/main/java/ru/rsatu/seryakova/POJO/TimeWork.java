package ru.rsatu.seryakova.POJO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TimeWork {
    private Integer week;
    private String monday;
    private String sunday;

    public TimeWork() {}
    public TimeWork(Integer week, String monday, String sunday) {
        this.week = week;
        this.monday = monday;
        this.sunday = sunday;
    }

    //дата понедельника
    public LocalDate getMonday() {
        LocalDate locDate;
        if (monday != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            locDate = LocalDate.parse(monday, formatter);
        } else {
            locDate = null;
        }
        return locDate;
    }
    //дата воскресенья
    public LocalDate getSunday() {
        LocalDate locDate;
        if (sunday != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            locDate = LocalDate.parse(sunday, formatter);
        } else {
            locDate = null;
        }
        return locDate;
    }

    //----------получение № недели
    public Integer getWeek() {
        return week;
    }
    public Integer getWeek(LocalDate today) {
        Double numWeek;//номер недели
        Long numDay;//количество дней с начала семеста
        if (today.getYear() != 1900) {
            LocalDate semester1 = LocalDate.of(today.getYear(), 9, 1);//начало осеннего семестра
            numDay = ChronoUnit.DAYS.between(semester1, today);//кол-во дней, кот. прошло с 1 сентября
            if (numDay < -5) { //весенний семестр
                semester1 = LocalDate.parse(getStartSem( LocalDate.of(today.getYear(), 1, 1))); //начало весеннего семестра
                numDay = ChronoUnit.DAYS.between(semester1, today);//кол-во дней, кот. прошло с начала весеннего сем
                if (numDay < 0) {
                    numWeek = 0d;
                } else {
                    numDay = numDay +1;
                    numWeek = Math.ceil(numDay / 7.0);
                }
            } else {//осенний семестр
                if (semester1.getDayOfWeek().toString().equals("SUNDAY")) {
                    semester1 = semester1.plusDays(1);
                    numDay = ChronoUnit.DAYS.between(semester1, today);
                    if (numDay < 0 ) {
                        numWeek = 0.0;
                    } else {
                        numDay = numDay + 1;
                        numWeek = Math.ceil(numDay / 7.0);
                    }
                } else {
                    numDay = ChronoUnit.DAYS.between(semester1, today);
                    if ((numDay > -6) && (numDay < 0)) {
                        numWeek = 1.0;

                    } else {
                        numDay = numDay + 1;
                        numWeek = Math.ceil(numDay / 7.0);
                    }
                }
                if (numWeek > 18) {
                    numWeek = 0.0;
                }}
        } else {
            numWeek = 99d; }
        return numWeek.intValue();
    }

    //-----------получение даты первого учебного дня выбранного семестра
    public LocalDate getFirstDay(String semester) {
        LocalDate firstDay;
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow")); //текущая дата
        if (semester.equals("autumn")) {
            firstDay = LocalDate.of(today.getYear(), 9, 1);//начало осеннего семестра
            if (firstDay.getDayOfWeek().toString().equals("SUNDAY")) {
                firstDay = firstDay.plusDays(1);
            }
        } else {
            firstDay = LocalDate.parse(getStartSem( LocalDate.of(today.getYear(), 1, 1))); //начало весеннего семестра
        }
        System.out.println(firstDay);
        return firstDay;
    }


    //расчет дня недели
    public String getDayOfWeek(String strDay) {
        String dow = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate today = LocalDate.parse(strDay, formatter);
        if (today.getYear() != 1900) {
            dow = today.getDayOfWeek().toString();
            if (dow.equals("MONDAY")) {
                dow = "Пн";
            } else if (dow.equals("TUESDAY")) {
                dow = "Вт";
            } else if (dow.equals("WEDNESDAY")) {
                dow = "Ср";
            } else if (dow.equals("THURSDAY")) {
                dow = "Чт";
            } else if (dow.equals("FRIDAY")) {
                dow = "Пт";
            } else if (dow.equals("SATURDAY")) {
                dow = "Сб";
            } else if (dow.equals("SUNDAY")) {
                dow = "Вс";
            }
        }
        return dow;
    }

    //полчение дат недели
    public List<String> getDayWeek(LocalDate monday) {
        ArrayList<String> DayWeek = new ArrayList<String>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DayWeek.add(0,formatter.format(monday));
        DayWeek.add(1,formatter.format(monday.plusDays(1)));
        DayWeek.add(2,formatter.format(monday.plusDays(2)));
        DayWeek.add(3,formatter.format(monday.plusDays(3)));
        DayWeek.add(4,formatter.format(monday.plusDays(4)));
        DayWeek.add(5,formatter.format(monday.plusDays(5)));
        DayWeek.add(6,formatter.format(monday.plusDays(6)));
        return DayWeek;
    }

    // получение пн недели;
    public LocalDate getMonWeek(LocalDate today) {
        if (today.getDayOfWeek().toString().equals("TUESDAY")) {
            today = today.minusDays(1);
        } else if (today.getDayOfWeek().toString().equals("WEDNESDAY")) {
            today = today.minusDays(2);
        } else if (today.getDayOfWeek().toString().equals("THURSDAY")) {
            today = today.minusDays(3);
        } else if (today.getDayOfWeek().toString().equals("FRIDAY")) {
            today = today.minusDays(4);
        } else if (today.getDayOfWeek().toString().equals("SATURDAY")) {
            today = today.minusDays(5);
        } else if (today.getDayOfWeek().toString().equals("SUNDAY")) {
            today = today.minusDays(6);
        }
        return today;
    }


    //начало семестра
    public String getStartSem(LocalDate today) {
        LocalDate semester = LocalDate.of(today.getYear(),2,11);
        Long numDay = ChronoUnit.DAYS.between(semester, today);
        if (numDay < 35){
            semester = LocalDate.of(today.getYear() - 1, 9, 1);//начало учебного года
            //начало весеннего семестра
            if (semester.getDayOfWeek().toString().equals("MONDAY")) {
                semester= semester.plusWeeks(23);
            } else if (semester.getDayOfWeek().toString().equals("TUESDAY")) {
                semester = semester.plusDays(6);
                semester= semester.plusWeeks(22);
            } else if (semester.getDayOfWeek().toString().equals("WEDNESDAY")) {
                semester = semester.plusDays(5);
                semester= semester.plusWeeks(22);
            } else if (semester.getDayOfWeek().toString().equals("THURSDAY")) {
                semester = semester.plusDays(4);
                semester= semester.plusWeeks(22);
            } else if (semester.getDayOfWeek().toString().equals("FRIDAY")) {
                semester = semester.plusDays(3);
                semester= semester.plusWeeks(22);
            } else if (semester.getDayOfWeek().toString().equals("SATURDAY")) {
                semester = semester.plusDays(2);
                semester= semester.plusWeeks(22);
            } else if (semester.getDayOfWeek().toString().equals("SUNDAY")) {
                semester = semester.plusDays(1);
                semester= semester.plusWeeks(23);
            }
        }else {
            semester = LocalDate.of(today.getYear(), 9, 1);//начало осеннего семестра
            if (semester.getDayOfWeek().toString().equals("SUNDAY")) {
                semester = semester.plusDays(1);
            }
        }

        return semester.toString();
    }

    //получение даты пары
    public LocalDate getDatePair(LocalDate startSem, Integer week, String dayWeek) {
        LocalDate date = startSem;
        if (week == 1) {
            switch (date.getDayOfWeek().toString()) {
                case "MONDAY":
                    switch (dayWeek) {
                        case "Вт":
                            date = date.plusDays(1);
                            break;
                        case "Ср":
                            date = date.plusDays(2);
                            break;
                        case "Чт":
                            date = date.plusDays(3);
                            break;
                        case "Пт":
                            date = date.plusDays(4);
                            break;
                        case "Сб":
                            date = date.plusDays(5);
                            break;
                    }
                    break;
                case "TUESDAY":
                    switch (dayWeek) {
                        case "Пн":
                            date = null;
                            break;
                        case "Ср":
                            date = date.plusDays(1);
                            break;
                        case "Чт":
                            date = date.plusDays(2);
                            break;
                        case "Пт":
                            date = date.plusDays(3);
                            break;
                        case "Сб":
                            date = date.plusDays(4);
                            break;
                    }
                    break;
                case "WEDNESDAY":
                    switch (dayWeek) {
                        case "Пн":
                        case "Вт":
                            date = null;
                            break;
                        case "Чт":
                            date = date.plusDays(1);
                            break;
                        case "Пт":
                            date = date.plusDays(2);
                            break;
                        case "Сб":
                            date = date.plusDays(3);
                            break;
                    }
                    break;
                case "THURSDAY":
                    switch (dayWeek) {
                        case "Пн":
                        case "Вт":
                        case "Ср":
                            date = null;
                            break;
                        case "Пт":
                            date = date.plusDays(1);
                            break;
                        case "Сб":
                            date = date.plusDays(2);
                            break;
                    }
                    break;
                case "FRIDAY":
                    switch (dayWeek) {
                        case "Пн":
                        case "Вт":
                        case "Ср":
                        case "Чт":
                            date = null;
                            break;
                        case "Сб":
                            date = date.plusDays(1);
                            break;
                    }
                    break;
                case "SATURDAY":
                    switch (dayWeek) {
                        case "Пн":
                        case "Чт":
                        case "Вт":
                        case "Ср":
                        case "Пт":
                            date = null;
                            break;
                    }
                    break;
            }
        } else {
            date = startSem.plusWeeks(week - 1);
            LocalDate monday = getMonWeek(date); // получение пн недели;
            switch (dayWeek) {
                case "Вт":
                    date = date.plusDays(1);
                    break;
                case "Ср":
                    date = date.plusDays(2);
                    break;
                case "Чт":
                    date = date.plusDays(3);
                    break;
                case "Пт":
                    date = date.plusDays(4);
                    break;
                case "Сб":
                    date = date.plusDays(5);
                    break;
            }
        }
        return date;
    }
}
