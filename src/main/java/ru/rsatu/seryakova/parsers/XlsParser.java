package ru.rsatu.seryakova.parsers;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import ru.rsatu.seryakova.POJO.ScheduleFromFile;
import ru.rsatu.seryakova.POJO.TimeWork;
import ru.rsatu.seryakova.exceptions.ParserException;
import ru.rsatu.seryakova.tables.Shedule;

import javax.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class XlsParser {
    int row (int row) {
        return row + 1;
    }

    public List<ScheduleFromFile> parse(String name) throws ParserException{

        StringBuilder result = new StringBuilder();
        InputStream in = null;
        HSSFWorkbook wb = null;
        try {
            //  System.out.println(name);
            in = new FileInputStream(name);
            wb = new HSSFWorkbook(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<ScheduleFromFile> schedule = new ArrayList<>();


        Sheet sheet = wb.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() != 0) {
                ScheduleFromFile curScheduleLine = new ScheduleFromFile();
                //             if (row.getCell(0).getCellType() != CellType.BLANK) {
                // if (row.getLastCellNum() != 11) throw new ParserException("Ошибка в cтроке " + (row.));
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        case 0:
                            curScheduleLine.setGroup(cell.toString());
                            break;
                        case 1:
                            curScheduleLine.setDayWeek(cell.toString());
                            break;
                        case 2:
//                                if(cell.getCellType() != CellType.NUMERIC) throw new ParserException("Ошибка в cтроке " + row(row.getRowNum()));
                            if ((cell.toString().charAt(2) != '0')) {
                                curScheduleLine.setNumber(String.valueOf(cell.toString().charAt(0)));
                                curScheduleLine.setWeek(row.getCell(3).toString());
                                curScheduleLine.setSubGroup(row.getCell(4).toString());
                                curScheduleLine.setType(row.getCell(5).toString());
                                curScheduleLine.setDis(row.getCell(6).toString());
                                curScheduleLine.setRoom(row.getCell(7).toString());
                                curScheduleLine.setTeacher(row.getCell(8).toString());
                                curScheduleLine.setRow(row.getRowNum());

                                for (int i = 0; i < schedule.size(); i++) {
                                    if (curScheduleLine.getGroup().equals(schedule.get(i).getGroup())) {
                                        if ((curScheduleLine.getDis().equals(schedule.get(i).getDis())) &&
                                                (curScheduleLine.getNumber().equals(schedule.get(i).getNumber()))&&
                                                (curScheduleLine.getDayWeek().equals(schedule.get(i).getDayWeek()))&&
                                                (curScheduleLine.getRoom().equals(schedule.get(i).getRoom()))&&
                                                (curScheduleLine.getSubGroup().equals(schedule.get(i).getSubGroup()))&&
                                                (curScheduleLine.getTeacher().equals(schedule.get(i).getTeacher()))&&
                                                (curScheduleLine.getType().equals(schedule.get(i).getType())))
                                            throw new ParserException("Занятие повторяется. Строка " + (row.getRowNum()));
                                    }
                                }

                                schedule.add(curScheduleLine);
                                curScheduleLine = new ScheduleFromFile();
                                curScheduleLine.setGroup(row.getCell(0).toString());
                                curScheduleLine.setDayWeek(row.getCell(1).toString());
                                curScheduleLine.setNumber(String.valueOf(cell.toString().charAt(2)));
                            } else {
                                curScheduleLine.setNumber(String.valueOf(cell.toString().charAt(0)));
                            }
                            break;
                        case 3:
                            curScheduleLine.setWeek(cell.toString());
                            break;
                        case 4:
                            curScheduleLine.setSubGroup(cell.toString());
                            break;
                        case 5:
                            curScheduleLine.setType(cell.toString());
                            break;
                        case 6:
                            curScheduleLine.setDis(cell.toString());
                            break;
                        case 7:
                            curScheduleLine.setRoom(cell.toString());
                            break;
                        case 8:
                            curScheduleLine.setTeacher(cell.toString());
                            curScheduleLine.setRow(row.getRowNum());

                            for (int i = 0; i < schedule.size(); i++) {
                                if (curScheduleLine.getGroup().equals(schedule.get(i).getGroup())) {
                                    if ((curScheduleLine.getDis().equals(schedule.get(i).getDis())) &&
                                            (curScheduleLine.getNumber().equals(schedule.get(i).getNumber()))&&
                                            (curScheduleLine.getDayWeek().equals(schedule.get(i).getDayWeek()))&&
                                            (curScheduleLine.getRoom().equals(schedule.get(i).getRoom()))&&
                                            (curScheduleLine.getSubGroup().equals(schedule.get(i).getSubGroup()))&&
                                            (curScheduleLine.getTeacher().equals(schedule.get(i).getTeacher()))&&
                                            (curScheduleLine.getType().equals(schedule.get(i).getType())))
                                        throw new ParserException("Занятие повторяется. Строка " + (row.getRowNum()));
                                }
                            }

                            schedule.add(curScheduleLine);
                            break;

                    }

                }

            }

        }
        return schedule;
    }

    public void exportToBD(List<ScheduleFromFile> tempSched, String semester, EntityManager em) throws ParserException, IOException{
        String str;
        Shedule pair = new Shedule();
        TimeWork timeWork = new TimeWork();
        LocalDate firstDay = timeWork.getFirstDay(semester);//дата начала семестра
        // System.out.println( semester+ " first day " + firstDay);
        List<Shedule> schedule = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        for (ScheduleFromFile scheduleFromXls : tempSched) {
          //  System.out.println("row " + scheduleFromXls.getRow());
            try {
                if ((scheduleFromXls.getWeek().indexOf('-') != -1) && (scheduleFromXls.getWeek().indexOf('/') == -1)) { //m-n чет/неч/все

                    Integer curWeek = Integer.parseInt((scheduleFromXls.getWeek().substring(0,
                            scheduleFromXls.getWeek().indexOf('-'))));
                    Integer lastWeek = Integer.parseInt(scheduleFromXls.getWeek().substring(scheduleFromXls.getWeek().
                            indexOf('-') + 1, scheduleFromXls.getWeek().indexOf(' ')));
                    String period = scheduleFromXls.getWeek().substring(scheduleFromXls.getWeek().indexOf(' ') + 1,
                            scheduleFromXls.getWeek().length());
                    if ((period.equals("все")) || (period.equals("чет")) || (period.equals("неч"))) {
                        //System.out.println(tempSched.get(i).getWeek());
                        while (curWeek <= lastWeek) {
                            final LocalDate datePair = timeWork.getDatePair(firstDay, curWeek, scheduleFromXls.getDayWeek());
                            if (datePair != null) {
                                String dateStr = datePair.format(formatter);
//                                if (scheduleFromXls.getDayWeek().equals("Ср")) {
//                                    System.out.println("number " + scheduleFromXls.getNumber());
//                                }
                                schedule.add(addPair(dateStr, scheduleFromXls, scheduleFromXls.getSubGroup()));
                            }
                            if (period.equals("все")) {
                                curWeek = curWeek + 1;
                            } else if ((period.equals("чет")) || (period.equals("неч"))) {
                                curWeek = curWeek + 2;
                            }
                        }
                    } else throw new ParserException("Ошибка в cтроке " + scheduleFromXls.getRow());
                } else if ((scheduleFromXls.getWeek().indexOf('-') == 1) && (scheduleFromXls.getWeek().indexOf('/') != -1)) { //n-m неч/l-p чет
                    int k = 1;
                    Integer curWeek = Integer.parseInt((scheduleFromXls.getWeek().substring(0, scheduleFromXls.getWeek()
                            .indexOf('-'))));
                    Integer lastWeek = Integer.parseInt(scheduleFromXls.getWeek().substring(scheduleFromXls.getWeek()
                            .indexOf('-') + 1, scheduleFromXls.getWeek().indexOf(' ')));
                    String subGr = scheduleFromXls.getSubGroup().substring(0, scheduleFromXls.getSubGroup().indexOf('/'));
                    while (k <= 2) {
                        while (curWeek <= lastWeek) {
                            final LocalDate datePair = timeWork.getDatePair(firstDay, curWeek, scheduleFromXls.getDayWeek());
                            if (datePair != null) {
                                // System.out.println(tempSched.get(i).getSubGroup() + " " + subGr);
                                String dateStr = datePair.format(formatter);
                                schedule.add(addPair(dateStr, scheduleFromXls, subGr));
                            }
                            curWeek = curWeek + 2;
                        }
                        k = k + 1;
                        str = scheduleFromXls.getWeek().substring(scheduleFromXls.getWeek().indexOf('/') + 1,
                                scheduleFromXls.getWeek().length());
                        curWeek = Integer.parseInt(str.substring(str.indexOf('/') + 1, str.indexOf('-')));
                        lastWeek = Integer.parseInt(str.substring(str.indexOf('-') + 1, str.indexOf(' ')));
                        subGr = scheduleFromXls.getSubGroup().substring(scheduleFromXls.getSubGroup().indexOf('/') + 1,
                                scheduleFromXls.getSubGroup().length());
                    }
                } else if ((scheduleFromXls.getWeek().indexOf('-') == -1) && (scheduleFromXls.getWeek().indexOf('/') != -1)) {  //4,6,8,10,12/5,7,9,11,13
                    str = scheduleFromXls.getWeek().substring(0, scheduleFromXls.getWeek().indexOf('/'));
                    String[] weeks = str.split(",");

                    String subGr = scheduleFromXls.getSubGroup().substring(0, scheduleFromXls.getSubGroup().indexOf('/'));
                    int k = 1;
                    while (k <= 2) {
                        for (int n = 0; n < weeks.length; n++) {
                            int week = Integer.parseInt(weeks[n]);
                            final LocalDate datePair = timeWork.getDatePair(firstDay, week, scheduleFromXls.getDayWeek());
                            if (datePair != null) {
                                String dateStr = datePair.format(formatter);
                                schedule.add(addPair(dateStr, scheduleFromXls, subGr));
                            }
                        }
                        k++;
                        str = scheduleFromXls.getWeek().substring(scheduleFromXls.getWeek().indexOf('/') + 1,
                                scheduleFromXls.getWeek().length());
                        weeks = str.split(",");
                        subGr = scheduleFromXls.getSubGroup().substring(scheduleFromXls.getSubGroup().indexOf('/') + 1,
                                scheduleFromXls.getSubGroup().length());
                    }
                } else if (((scheduleFromXls.getWeek().indexOf(',') != -1))
                        && (scheduleFromXls.getWeek().indexOf('/') == -1)) { //2,3,4
                    String[] weeks = scheduleFromXls.getWeek().split(",");
                    for (int n = 0; n < weeks.length; n++) {
                        int week = Integer.parseInt(weeks[n]);
                        final LocalDate datePair = timeWork.getDatePair(firstDay, week, scheduleFromXls.getDayWeek());
                        if (datePair != null) {
                            String dateStr = datePair.format(formatter);
                            schedule.add(addPair(dateStr, scheduleFromXls, scheduleFromXls.getSubGroup()));

                        }
                    }
                } else if (scheduleFromXls.getWeek().indexOf('.') != -1) {
                    String[] weeks = scheduleFromXls.getWeek().split("[.]");
                    for (int n = 0; n < weeks.length; n++) {
                        int week = Integer.parseInt(weeks[n]);
                        final LocalDate datePair = timeWork.getDatePair(firstDay, week, scheduleFromXls.getDayWeek());
                        if (datePair != null) {
                            String dateStr = datePair.format(formatter);
                            schedule.add(addPair(dateStr, scheduleFromXls, scheduleFromXls.getSubGroup()));
                        }
                    }
                }
                else if (scheduleFromXls.getWeek() == null) {
                    throw new ParserException("Ошибка в cтроке " + scheduleFromXls.getRow());
                }
                else {
                    int week = Integer.parseInt(scheduleFromXls.getWeek());
                    final LocalDate datePair = timeWork.getDatePair(firstDay, week, scheduleFromXls.getDayWeek());
                    if (datePair != null) {
                        String dateStr = datePair.format(formatter);
                        schedule.add(addPair(dateStr, scheduleFromXls, scheduleFromXls.getSubGroup()));
                    }

                }
            } catch (Exception e) {
                if (scheduleFromXls.getRow() != null) {
                    throw new ParserException("Ошибка в cтроке " + scheduleFromXls.getRow());
                } else {
                    throw new ParserException("Ошибка в файле");
                }
            }
        }
        em.createQuery("delete from Shedule").executeUpdate();
        for (Shedule p: schedule) {
            em.persist(p);
        }
    }

    private Shedule addPair(String date, ScheduleFromFile pairFromXls, String subGroup) {
        Shedule pair = new Shedule();
        pair.setDate(date.toString());
        pair.setNameGroup(pairFromXls.getGroup());
        pair.setDayWeek(pairFromXls.getDayWeek());
        if (subGroup.length() != 0) {
            subGroup = subGroup.substring(0,1);
        }
        pair.setSubGroup(subGroup);
        pair.setNumberPair(Integer.parseInt(pairFromXls.getNumber()));
        pair.setDiscipline(pairFromXls.getDis());
        pair.setNumberPair(Integer.parseInt(pairFromXls.getNumber()));
        pair.setTypepair(pairFromXls.getType());
        pair.setTeacher(pairFromXls.getTeacher());
        pair.setRoom(pairFromXls.getRoom());
        return pair;
    }
}
