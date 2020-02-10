package ru.rsatu.seryakova;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import ru.rsatu.seryakova.pojo.ExportRasp;
import ru.rsatu.seryakova.utilites.ForAuth;
import ru.rsatu.seryakova.tables.Shedule;
import ru.rsatu.seryakova.utilites.TimeWork;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportFile {
    Font font = new Font();
    private static final Logger log = Logger.getLogger(ExportFile.class);

    public ExportFile() {
        //BaseFont bf = null; //подключаем файл шрифта, который поддерживает кириллицу
        try {
            BaseFont bf = BaseFont.createFont("times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            font = new Font(bf);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public File createFileXLS(EntityManager em) {
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet");

            //Cell cell;
            Row headerRow;
            String[] columns = {"Группа", "День недели", "№ пары", "Недели", "Подгруппа", "Вид", "Дисциплина", "Кабинет",
                    "Преподаватель", "Информация"};
            //
            HSSFCellStyle style = createStyleForTitle(workbook);
            headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(style);
            }
            List<ExportRasp> rasp = new ArrayList<>();
            List<String> listGroup = em.createQuery("SELECT MT.nameGroup FROM Shedule MT group by MT.nameGroup", String.class).getResultList();


            for (int i = 0; i < listGroup.size(); i++) {
                log.info(listGroup.get(i));
                log.info(rasp.size());
                List<ExportRasp> tempRasp = getRasp(listGroup.get(i), "null", em);
                tempRasp = sortOfDayWeek(tempRasp);
                rasp.addAll(tempRasp);
            }
            int rowNum = 1;

            for (int i = 0; i < rasp.size(); i++) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0)
                        .setCellValue(rasp.get(i).getGroup());
                row.createCell(1)
                        .setCellValue(rasp.get(i).getDayWeek());
                row.createCell(2, CellType.NUMERIC)
                        .setCellValue(rasp.get(i).getNumber());
                row.createCell(3)
                        .setCellValue(rasp.get(i).getWeek());
                row.createCell(4)
                        .setCellValue(rasp.get(i).getSubGroup());
                row.createCell(5)
                        .setCellValue(rasp.get(i).getType());
                row.createCell(6)
                        .setCellValue(rasp.get(i).getDis());
                row.createCell(7)
                        .setCellValue(rasp.get(i).getRoom());
                row.createCell(8)
                        .setCellValue(rasp.get(i).getTeacher());
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            FileOutputStream fileOut = new FileOutputStream("rasp.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            File file = new File("rasp.xlsx");
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


        public File createFilePdf(String teacher, String group, String authorization, EntityManager em) {
        Document doc = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("Rasp.pdf"));
            doc.open();
            //log.info(getClass().getClassLoader().getResource("times.ttf"));
            log.info("Экспорт в PDF");
            if (group.equals("null")) {
                if ((!authorization.equals("Bearer null"))) {
                    ForAuth forAuth = new ForAuth();
                    log.info(authorization);
                    if ((forAuth.parseToken(authorization, em).getRole().equals("teacher")) && (teacher.equals("null"))) {
                        teacher = em.createQuery("SELECT T.FIO FROM Teachers T left join  Users U on T.userId = U.userId " +
                                "WHERE U.login like :login", String.class)
                                .setParameter("login", forAuth.parseToken(authorization, em).getLogin())
                                .getSingleResult();
                        doc.add(new Paragraph(teacher, font));
                    } else {
                        doc.add(new Paragraph(teacher, font));
                    }
                } else {
                    doc.add(new Paragraph(teacher, font));
                }


            } else {
                doc.add(new Paragraph(group, font));
            }
            List<ExportRasp> rasp = getRasp(group, teacher, em);
            rasp = sortOfDayWeek(rasp);
            log.info(teacher);

            PdfPTable table = new PdfPTable(8); // 7 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            //Set Column widths
            float[] columnWidths = {0.7f, 0.5f, 1.2f, 0.7f, 0.5f, 2f, 1.2f, 2f};
            table.setWidths(columnWidths);

            ExportFile pdf = new ExportFile();
            List<PdfPCell> listCell = pdf.createHeaders(rasp, teacher);
            for (int i = 0; i < listCell.size(); i++) {
                table.addCell(listCell.get(i));
            }
            listCell = pdf.createLines(rasp, teacher);
            for (int i = 0; i < listCell.size(); i++) {
                table.addCell(listCell.get(i));
            }
            doc.add(table);
            doc.close();
            writer.close();
            File file = new File("Rasp.pdf");
            return file;
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public HSSFCellStyle createStyleForTitle(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private List<ExportRasp> sortOfDayWeek(List<ExportRasp> rasp) {
        List<ExportRasp> tempRasp = new ArrayList<>();
        String[] dayWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        for (String s : dayWeek) {
            for (ExportRasp exportRasp : rasp) {
                if (exportRasp.getDayWeek().equals(s)) {
                    tempRasp.add(exportRasp);
                }
            }
        }
        return tempRasp;
    }

    public List<PdfPCell> createLines(List<ExportRasp> rasp, String teacher) {

        List<PdfPCell> listCell = new ArrayList<>();

        for (int i = 0; i < rasp.size(); i++) {
            PdfPCell cell1 = new PdfPCell(new Phrase(rasp.get(i).getDayWeek(), font));
            cell1.setBorderColor(BaseColor.BLACK);
            cell1.setPaddingLeft(1);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell1);

            PdfPCell cell2 = new PdfPCell(new Paragraph(rasp.get(i).getNumber(), font));
            cell2.setBorderColor(BaseColor.BLACK);
            cell2.setPaddingLeft(2);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell2);

            PdfPCell cell8 = new PdfPCell(new Paragraph(rasp.get(i).getWeek(), font));
            cell8.setBorderColor(BaseColor.BLACK);
            cell8.setPaddingLeft(2);
            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell8);

            PdfPCell cell3 = new PdfPCell(new Paragraph(rasp.get(i).getSubGroup(), font));
            cell3.setBorderColor(BaseColor.BLACK);
            cell3.setPaddingLeft(2);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell3);

            PdfPCell cell4 = new PdfPCell(new Paragraph(rasp.get(i).getType(), font));
            cell4.setBorderColor(BaseColor.BLACK);
            cell4.setPaddingLeft(1);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell4);

            PdfPCell cell5 = new PdfPCell(new Paragraph(rasp.get(i).getDis(), font));
            cell5.setBorderColor(BaseColor.BLACK);
            cell5.setPaddingLeft(2);
            cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell5);

            PdfPCell cell6 = new PdfPCell(new Paragraph(rasp.get(i).getRoom(), font));
            cell6.setBorderColor(BaseColor.BLACK);
            cell6.setPaddingLeft(2);
            cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
            listCell.add(cell6);

            if (teacher.equals("null")) {
                PdfPCell cell7 = new PdfPCell(new Paragraph(rasp.get(i).getTeacher(), font));
                cell7.setBorderColor(BaseColor.BLACK);
                cell7.setPaddingLeft(2);
                cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
                listCell.add(cell7);
            } else {
                PdfPCell cell7 = new PdfPCell(new Paragraph(rasp.get(i).getGroup(), font));
                cell7.setBorderColor(BaseColor.BLACK);
                cell7.setPaddingLeft(2);
                cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
                listCell.add(cell7);
            }
        }
        return listCell;
    }

    public List<PdfPCell> createHeaders(List<ExportRasp> rasp, String teacher) {
        List<PdfPCell> listCell = new ArrayList<>();
        PdfPCell cell1 = new PdfPCell(new Phrase("День недели", font));
        cell1.setBorderColor(BaseColor.BLACK);
        cell1.setPaddingLeft(2);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell1);

        PdfPCell cell2 = new PdfPCell(new Paragraph("№", font));
        cell2.setBorderColor(BaseColor.BLACK);
        cell2.setPaddingLeft(2);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell2);

        PdfPCell cell8 = new PdfPCell(new Paragraph("Недели", font));
        cell8.setBorderColor(BaseColor.BLACK);
        cell8.setPaddingLeft(2);
        cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell8);

        PdfPCell cell3 = new PdfPCell(new Paragraph("Подгр.", font));
        cell3.setBorderColor(BaseColor.BLACK);
        cell3.setPaddingLeft(2);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell3);

        PdfPCell cell4 = new PdfPCell(new Paragraph("Вид", font));
        cell4.setBorderColor(BaseColor.BLACK);
        cell4.setPaddingLeft(1);
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell4);

        PdfPCell cell5 = new PdfPCell(new Paragraph("Дисциплина", font));
        cell5.setBorderColor(BaseColor.BLACK);
        cell5.setPaddingLeft(5);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell5);

        PdfPCell cell6 = new PdfPCell(new Paragraph("Аудитория", font));
        cell6.setBorderColor(BaseColor.BLACK);
        cell6.setPaddingLeft(10);
        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        listCell.add(cell6);

        PdfPCell cell7 = new PdfPCell();
        if (teacher.equals("null")) {
            cell7 = new PdfPCell(new Paragraph("Преподаватель", font));
            cell7.setBorderColor(BaseColor.BLACK);
            cell7.setPaddingLeft(8);
            cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
        } else {
            cell7 = new PdfPCell(new Paragraph("Группа", font));
            cell7.setBorderColor(BaseColor.BLACK);
            cell7.setPaddingLeft(8);
            cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }
        listCell.add(cell7);
        return  listCell;
    }
    public void createList(List<ExportRasp> rasp, String teacher) {

        PdfPTable table = new PdfPTable(8); // 7 columns.
        table.setWidthPercentage(100); //Width 100%
        table.setSpacingBefore(10f); //Space before table
        table.setSpacingAfter(10f); //Space after table

        //Set Column widths
        float[] columnWidths = {0.7f, 0.5f, 1.2f, 0.7f, 0.5f, 2f, 1.2f, 2f};
        try {
            table.setWidths(columnWidths);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        PdfPCell cell1 = new PdfPCell(new Phrase("День недели", font));
        cell1.setBorderColor(BaseColor.BLACK);
        cell1.setPaddingLeft(2);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell2 = new PdfPCell(new Paragraph("№", font));
        cell2.setBorderColor(BaseColor.BLACK);
        cell2.setPaddingLeft(2);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell8 = new PdfPCell(new Paragraph("Недели", font));
        cell8.setBorderColor(BaseColor.BLACK);
        cell8.setPaddingLeft(2);
        cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell3 = new PdfPCell(new Paragraph("Подгр.", font));
        cell3.setBorderColor(BaseColor.BLACK);
        cell3.setPaddingLeft(2);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell4 = new PdfPCell(new Paragraph("Вид", font));
        cell4.setBorderColor(BaseColor.BLACK);
        cell4.setPaddingLeft(1);
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell5 = new PdfPCell(new Paragraph("Дисциплина", font));
        cell5.setBorderColor(BaseColor.BLACK);
        cell5.setPaddingLeft(5);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell6 = new PdfPCell(new Paragraph("Аудитория", font));
        cell6.setBorderColor(BaseColor.BLACK);
        cell6.setPaddingLeft(10);
        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell7 = new PdfPCell();
        if (teacher.equals("null")) {
            cell7 = new PdfPCell(new Paragraph("Преподаватель", font));
            cell7.setBorderColor(BaseColor.BLACK);
            cell7.setPaddingLeft(8);
            cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
        } else {
            cell7 = new PdfPCell(new Paragraph("Группа", font));
            cell7.setBorderColor(BaseColor.BLACK);
            cell7.setPaddingLeft(8);
            cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell8);
        table.addCell(cell3);
        table.addCell(cell4);
        table.addCell(cell5);
        table.addCell(cell6);
        table.addCell(cell7);



        for (int i = 0; i < rasp.size(); i++) {
            cell1 = new PdfPCell(new Phrase(rasp.get(i).getDayWeek(), font));
            cell1.setBorderColor(BaseColor.BLACK);
            cell1.setPaddingLeft(1);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell2 = new PdfPCell(new Paragraph(rasp.get(i).getNumber(), font));
            cell2.setBorderColor(BaseColor.BLACK);
            cell2.setPaddingLeft(2);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell8 = new PdfPCell(new Paragraph(rasp.get(i).getWeek(), font));
            cell8.setBorderColor(BaseColor.BLACK);
            cell8.setPaddingLeft(2);
            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell3 = new PdfPCell(new Paragraph(rasp.get(i).getSubGroup(), font));
            cell3.setBorderColor(BaseColor.BLACK);
            cell3.setPaddingLeft(2);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell4 = new PdfPCell(new Paragraph(rasp.get(i).getType(), font));
            cell4.setBorderColor(BaseColor.BLACK);
            cell4.setPaddingLeft(1);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell5 = new PdfPCell(new Paragraph(rasp.get(i).getDis(), font));
            cell5.setBorderColor(BaseColor.BLACK);
            cell5.setPaddingLeft(2);
            cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell6 = new PdfPCell(new Paragraph(rasp.get(i).getRoom(), font));
            cell6.setBorderColor(BaseColor.BLACK);
            cell6.setPaddingLeft(2);
            cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (teacher.equals("null")) {
                cell7 = new PdfPCell(new Paragraph(rasp.get(i).getTeacher(), font));
                cell7.setBorderColor(BaseColor.BLACK);
                cell7.setPaddingLeft(2);
                cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
            } else {
                cell7 = new PdfPCell(new Paragraph(rasp.get(i).getGroup(), font));
                cell7.setBorderColor(BaseColor.BLACK);
                cell7.setPaddingLeft(2);
                cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
            }

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell8);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            table.addCell(cell7);
        }
    }

    private List<ExportRasp> getRasp(String group, String teacher, EntityManager em) {
        TimeWork TW = new TimeWork();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String week = "";
        Integer index = null;
        List<Shedule> list = em.createQuery("SELECT MT FROM Shedule MT   WHERE MT.nameGroup IN :NG and" +
                " MT.teacher IN :T ORDER BY MT.numberPair, CAST(MT.date as date) ", Shedule.class)
                .setParameter("NG", !group.equals("null")
                        ? group
                        : em.createQuery("SELECT DISTINCT MT.nameGroup FROM Shedule MT").getResultList()
                )
                .setParameter("T", !teacher.equals("null")
                        ? teacher
                        : em.createQuery("SELECT DISTINCT MT.teacher FROM Shedule MT").getResultList()
                )//преподователь
                .getResultList();
        List<ExportRasp> rasp = new ArrayList<>();
        ExportRasp pair = new ExportRasp();
        pair.setGroup(list.get(0).getNameGroup());
        pair.setDayWeek(list.get(0).getDayWeek());
        pair.setDis(list.get(0).getDiscipline());
        pair.setNumber(list.get(0).getNumberPair().toString());
        pair.setRoom(list.get(0).getRoom());
        pair.setSubGroup(list.get(0).getSubGroup());
        pair.setTeacher(list.get(0).getTeacher());
        pair.setType(list.get(0).getTypepair());
        LocalDate day = LocalDate.parse(list.get(1).getDate(), formatter);
        pair.setWeek(TW.getWeek(day).toString());
        rasp.add(pair);
        rasp.remove(0);

        for (Shedule shedule : list) {
            index = null;
            for (int i = 0; i < rasp.size(); i++) {

                if (rasp.get(i).getDayWeek().equals(shedule.getDayWeek()) &&
                        (rasp.get(i).getDis().equals(shedule.getDiscipline())) &&
                        (rasp.get(i).getGroup().equals(shedule.getNameGroup())) &&
                        (rasp.get(i).getNumber().equals(shedule.getNumberPair().toString())) &&
                        (rasp.get(i).getRoom().equals(shedule.getRoom())) &&
                        (rasp.get(i).getTeacher().equals(shedule.getTeacher())) &&
                        (rasp.get(i).getType().equals(shedule.getTypepair()))) {
//                    log.info(" "+ rasp.size()+" "+ i +" " +
//                            rasp.get(i).getDayWeek().equals(shedule.getDayWeek()) +" " +
//                            (rasp.get(i).getDis().equals(shedule.getDiscipline())) +" " +
//                            (rasp.get(i).getGroup().equals(shedule.getNameGroup())) +" " +
//                            (rasp.get(i).getNumber().equals(shedule.getNumberPair().toString())) +" " +
//                            (rasp.get(i).getRoom().equals(shedule.getRoom())) +" " +
//                            (rasp.get(i).getTeacher().equals(shedule.getTeacher())) +" " +
//                            (rasp.get(i).getType().equals(shedule.getTypepair())));
                    if ((rasp.get(i).getSubGroup() == null) && (shedule.getSubGroup() == null)){
                        index = i;
                    } else if ((rasp.get(i).getSubGroup() != null) && (shedule.getSubGroup() != null)){
                        if (rasp.get(i).getSubGroup().equals(shedule.getSubGroup())) {
                            index = i;
                        }
                    }

                }
            }
            if (index == null) {

                pair = new ExportRasp();
                pair.setGroup(shedule.getNameGroup());
                pair.setDayWeek(shedule.getDayWeek());
                pair.setDis(shedule.getDiscipline());
                pair.setNumber(shedule.getNumberPair().toString());
                pair.setRoom(shedule.getRoom());
                pair.setSubGroup(shedule.getSubGroup());
                pair.setTeacher(shedule.getTeacher());
                pair.setType(shedule.getTypepair());
                day = LocalDate.parse(shedule.getDate(), formatter);
                pair.setWeek(TW.getWeek(day).toString());
                rasp.add(pair);
            } else {
                day = LocalDate.parse(shedule.getDate(), formatter);
                week = rasp.get(index).getWeek();
                week = week + "," + TW.getWeek(day).toString();
                rasp.get(index).setWeek(week);
            }
        }

        for (ExportRasp curPair : rasp) {
            String[] weeks = curPair.getWeek().split(",");
            if (weeks.length == 18) {
                curPair.setWeek("1-18 все");
            }
            int k = 0;
            int k1 = 0;
            for (int n = 0; n < weeks.length - 1; n++) {
                int w = Integer.parseInt(weeks[n]) +2;
                if (Integer.parseInt(weeks[n+1]) == w) {
                    k++;
                } else {
                    w = Integer.parseInt(weeks[n]) +1;
                    if (Integer.parseInt(weeks[n+1]) == w) {
                        k1++;
                    }

                }
            }
            if (k > 3) {
                if (Integer.parseInt(weeks[0]) == 1) {
                    curPair.setWeek(  weeks[0] + "-" + weeks[weeks.length - 1] + " неч");
                } else {
                    curPair.setWeek( weeks[0] + "-" + weeks[weeks.length - 1] + " чет");
                }
            }else if((k1 > 3) && (k1 == weeks.length-1)) {
                curPair.setWeek( weeks[0] + "-" + weeks[weeks.length - 1] + " все");
            }
        }

        return rasp;
    }
}
