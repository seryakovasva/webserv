package ru.rsatu.seryakova;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import ru.rsatu.seryakova.POJO.ExportRasp;
import ru.rsatu.seryakova.POJO.ObjSearch;
import ru.rsatu.seryakova.POJO.Search;
import ru.rsatu.seryakova.POJO.TimeWork;
import ru.rsatu.seryakova.Utilites.ForAuth;
import ru.rsatu.seryakova.tables.Shedule;
import ru.rsatu.seryakova.tables.Teachers;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Path("/main")
public class MainClass {
    @Inject
    EntityManager em;
    private static final Logger log = Logger.getLogger(MainClass.class);

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        log.info("hello");
        return "hello kuska";

    }

    //Тестрирование сервиса
    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTest(){
        Gson gson = new Gson();
        List<Shedule> list = em.createQuery("SELECT t FROM Shedule t", Shedule.class)
                .getResultList();
        //em.createQuery("INSERT INTO shedule (day, discipline) VALUES ('1994-11-29', 'Hayward')");
        return Response
                .status(Response.Status.OK)//удалена дата текущего дня
                .entity(gson.toJson(list.size()))
                .build();//выполнить;
    }

    //запрос на получение недели и дат
    @POST
    @Path("/getToWeek")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    @Consumes(value={"application/json"})
    public Response getToWeek(String s) {
        Gson gson = new Gson();
        Integer week = 0;
        LocalDate monday = LocalDate.now(ZoneId.of("Europe/Moscow"));
        LocalDate sunday = LocalDate.now(ZoneId.of("Europe/Moscow"));
        TimeWork time = gson.fromJson(s, TimeWork.class);
        LocalDate actDay = LocalDate.now(ZoneId.of("Europe/Moscow")); //текущая дата

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if ((time.getMonday() == null) & (time.getWeek() == null) & (time.getSunday() == null)) { // получение недели и пн,вс при инициализации
            week = time.getWeek(actDay); //получение № недели
            monday = time.getMonWeek(actDay); // получение пн недели;
            sunday = monday.plusDays(6);// получение вс недели
            System.out.println("tek week " + week);

        } else if ((time.getMonday() != null) & (time.getWeek() == null) & (time.getSunday() == null)) {//получение дня при выборе дня в календаре
            System.out.println("kalendar" + time.getMonday());
            week = time.getWeek(time.getMonday());
            monday = time.getMonWeek(time.getMonday()); // получение пн недели
            sunday = monday.plusDays(6);// получение вс недели
            System.out.println("kalendar");
        } else //след. неделя
            if ((time.getWeek() != null) &(time.getMonday() != null) & (time.getSunday() == null)) {//неделя, понедельнк

                //week = time.getWeek() + 1;
                monday = time.getMonday().plusDays(7);
                week = time.getWeek(monday);
                log.info(week + "nedely");
                sunday = monday.plusDays(6);
                System.out.println("sled week");
            } else if ((time.getWeek() != null) &(time.getMonday() == null) & (time.getSunday() != null)) {//пред. неделя
                //week = time.getWeek() - 1;
                monday = time.getSunday().minusDays(13);
                week = time.getWeek(monday);
                log.info(week + "nedely");
                sunday = monday.plusDays(6);
                System.out.println("back week s " + sunday + "mon " + monday.toString());
            }

        return Response
                .status(Response.Status.OK)//удалена дата текущего дня
                .entity(gson.toJson(new TimeWork(week, formatter.format(monday), formatter.format(sunday))))
                .build();//выполнить;
    }

    //получение параметров группы или преподавателя
    @POST
    @Path("/getParam")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    @Consumes(value={"application/json"})
    public Response getParam(String s) {
        Gson gson = new Gson();
        log.info("получение параметров группы или преподавателя");
        ObjSearch search = gson.fromJson(s, ObjSearch.class);
        log.info(s);
        List<Search> list = em.createQuery("SELECT NEW ru.rsatu.seryakova.POJO.Search(MT.nameGroup, " +
                        "MT.room, MT.teacher, MT.discipline) FROM Shedule MT " +
                        "where MT.teacher in :teach and MT.nameGroup in :name",
                Search.class)
                .setParameter("name",search.getGroup() != null
                        ? search.getGroup()
                        : em.createQuery("SELECT DISTINCT MT.nameGroup FROM Shedule MT").getResultList()
                )
                .setParameter("teach",search.getTeacher() != null
                        ? search.getTeacher()
                        : em.createQuery("SELECT DISTINCT MT.teacher FROM Shedule MT").getResultList()
                )
                .getResultList();

        return Response
                .status(Response.Status.OK)
                .entity(gson.toJson(list))
                .build();//выполнить
    }

    //Поиск
    @POST
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response search(String s, @HeaderParam("authorization") String authorization) {
        Gson gson = new Gson();
        log.info("Поиск");
        log.info(s);
        log.info(authorization);
        ObjSearch search = gson.fromJson(s, ObjSearch.class);
        TimeWork TW = gson.fromJson(s, TimeWork.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<String> group = new ArrayList<>();
        List<String> teacher = new ArrayList<>();
//            log.info(teacher);
//            log.info(search.getNumberPar());
        //log.info(search.getNumberPar());
        if (search.getDiscipline() != null) {
            if (search.getDiscipline().size() == 0) {
                search.setDiscipline(null);
            }
        }
        if (search.getRoom()!= null) {
            if (search.getRoom().size() == 0) {
                search.setRoom(null);
            }
        }
        if (search.getNumberPar() != null) {
            if (search.getNumberPar().size() == 0) {
                search.setNumberPar(null);
            }
        }
        if (search.getGrName() != null) {
            if (search.getGrName().size() == 0) {
                search.setGrName(null);
            }
        }
        if (search.getTeachers() != null) {
            if (search.getTeachers().size() == 0) {
                search.setTeachers(null);
            }
        }
        if (search.getGroup() == null) {
            group = search.getGrName();
        } else {
            group.add(search.getGroup());
        }
        if (search.getTeacher() == null) {
            teacher = search.getTeachers();
        } else {
            teacher.add(search.getTeacher());
        }
        List<Shedule> list = em.createQuery("SELECT MT FROM Shedule MT WHERE MT.nameGroup IN :NG and " +
                "MT.teacher IN :T and MT.discipline IN :Dis and MT.room IN :room and MT.numberPair IN :NP and MT.date in :day " +
                "ORDER BY CAST(MT.date as date), MT.numberPair ", Shedule.class)
                .setParameter("day", search.getDate() != null
                        ? formatter.format(search.getDate())
                        : TW.getDayWeek(TW.getMonday())
                )
                .setParameter("NG", group != null
                        ? group
                        : em.createQuery("SELECT DISTINCT MT.nameGroup FROM Shedule MT").getResultList()
                )
                .setParameter("T", teacher != null
                        ? teacher
                        : em.createQuery("SELECT DISTINCT MT.teacher FROM Shedule MT").getResultList()
                )//преподователь
                .setParameter("Dis", search.getDiscipline() != null ?
                        search.getDiscipline() :
                        em.createQuery("SELECT DISTINCT MT.discipline FROM Shedule MT").getResultList()
                )//дисциплина
                .setParameter("room", search.getRoom() != null
                        ? search.getRoom()
                        : em.createQuery("SELECT DISTINCT MT.room FROM Shedule MT").getResultList()
                )//аудитория
                .setParameter("NP", search.getNumberPar() != null ?
                        search.getNumberPar() :
                        em.createQuery("SELECT DISTINCT MT.numberPair FROM Shedule MT").getResultList()
                )//номер пары
                .getResultList();

        // list = TW.sortirovka(list);
        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i).getDayWeek() == null)) {
                list.get(i).setDayWeek(TW.getDayOfWeek(list.get(i).getDate()));
            }
        }
        return Response
                .status(Response.Status.OK)
                .entity(gson.toJson(list))
                .build();//выполнить;

    }

    //инфа о преподавателе
    @POST
    @Path("/getInfo")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response getInfo(String s) {
        Gson gson = new Gson();
        Teachers t = gson.fromJson(s,Teachers.class);
        Teachers teacher = new Teachers();
        try {
            teacher = em.createQuery("SELECT T FROM Teachers T where T.FIO in :fio", Teachers.class)
                    .setParameter("fio", t.getFIO())
                    .getSingleResult();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.OK)
                    .build();
        }
        return Response
                .status(Response.Status.OK)
                .entity(gson.toJson(teacher.getLastName() + " " + teacher.getName() + " " + teacher.getMidleName()))
                .build();
    }

    @GET
    @Path("/exportPdf")
    @Produces("application/pdf")
    public Response exportPdff(String s,
                               @QueryParam("group") String group,
                               @QueryParam("teacher") String teacher,
                               @HeaderParam("authorization") String authorization){
        log.info(group + " " + teacher);
        Document doc = new Document();
        Gson gson = new Gson();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("Rasp.pdf"));
            doc.open();


            BaseFont bf = BaseFont.createFont("/home/sseryakova/webAppProjs/webservice/src/main/resources/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); //подключаем файл шрифта, который поддерживает кириллицу
            Font font = new Font(bf);
            log.info(group.equals("null"));


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
            List<ExportRasp> rasp = getRasp(group, teacher);
            rasp = sortOfDayWeek(rasp);
            log.info(teacher);

            PdfPTable table = new PdfPTable(8); // 7 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            //Set Column widths
            float[] columnWidths = {0.7f, 0.5f, 1.2f, 0.7f, 0.5f, 2f, 1.2f, 2f};
            table.setWidths(columnWidths);

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


            doc.add(table);

            doc.close();
            writer.close();

            File file = new File("Rasp.pdf");
            Response.ResponseBuilder resp = Response.ok(file);
            resp.header("Content-Disposition", "attachment; filename=DisplayName-Rasp.pdf");
            return resp.build();

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    @GET
    @Path("/exportXsl")
    @Produces("application/pdf")
    public Response exportXsl(String s,
                              @HeaderParam("authorization") String authorization) throws IOException, DocumentException {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("sheet");



        //Cell cell;
        Row headerRow;
        String[] columns = {"Группа", "День недели", "№ пары", "Недели", "Подгруппа", "Вид", "Дисциплина", "Кабинет",
                "Преподаватель", "Информация"};
        //
        HSSFCellStyle style = createStyleForTitle(workbook);

        headerRow = sheet.createRow(0);

        for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(style);
        }
        List<ExportRasp> rasp = new ArrayList<>();
        List<String> listGroup =  em.createQuery("SELECT MT.nameGroup FROM Shedule MT group by MT.nameGroup", String.class).getResultList();


        for(int i = 0; i < listGroup.size(); i++) {
            log.info(listGroup.get(i));
            log.info(rasp.size());
            List<ExportRasp> tempRasp = getRasp(listGroup.get(i), "null");
            tempRasp = sortOfDayWeek(tempRasp);
            rasp.addAll(tempRasp);
        }

        int rowNum = 1;

        for(int i = 0; i < rasp.size(); i++) {
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
        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        FileOutputStream fileOut = new FileOutputStream("rasp.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        File file = new File("rasp.xlsx");
        Response.ResponseBuilder resp = Response.ok(file);
        resp.header("Content-Disposition", "attachment; filename=DisplayName-Rasp.pdf");
        return resp.build();

    }

    private List<ExportRasp> getRasp(String group, String teacher) {
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
//        log.info("date " + pair.getWeek() +" "+ day + (pair.getSubGroup() == null));
//
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
                    log.info(" "+ rasp.size()+" "+ i +" " +
                            rasp.get(i).getDayWeek().equals(shedule.getDayWeek()) +" " +
                            (rasp.get(i).getDis().equals(shedule.getDiscipline())) +" " +
                            (rasp.get(i).getGroup().equals(shedule.getNameGroup())) +" " +
                            (rasp.get(i).getNumber().equals(shedule.getNumberPair().toString())) +" " +
                            (rasp.get(i).getRoom().equals(shedule.getRoom())) +" " +
                            (rasp.get(i).getTeacher().equals(shedule.getTeacher())) +" " +
                            (rasp.get(i).getType().equals(shedule.getTypepair())));
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

    List<ExportRasp> sortOfDayWeek(List<ExportRasp> rasp) {
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

}

