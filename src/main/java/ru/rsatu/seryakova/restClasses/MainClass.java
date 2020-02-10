package ru.rsatu.seryakova.restClasses;

import com.google.gson.Gson;
import com.itextpdf.text.*;
import org.apache.log4j.Logger;
import ru.rsatu.seryakova.ExportFile;
import ru.rsatu.seryakova.pojo.MyTime;
import ru.rsatu.seryakova.pojo.SearchPair;
import ru.rsatu.seryakova.pojo.infOfGroup.Faculty;
import ru.rsatu.seryakova.pojo.ObjSearch;
import ru.rsatu.seryakova.pojo.Search;
import ru.rsatu.seryakova.tables.InfOfGroups;
import ru.rsatu.seryakova.tables.Shedule;
import ru.rsatu.seryakova.tables.Teachers;
import ru.rsatu.seryakova.utilites.TimeWork;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


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
        return "hello";

    }

    //Тестрирование сервиса
    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTest(){
        Gson gson = new Gson();
        List<Shedule> list = em.createQuery("SELECT t FROM Shedule t", Shedule.class)
                .getResultList();
        return Response
                .status(Response.Status.OK)//удалена дата текущего дня
                .entity(list)
                .build();//выполнить;
    }

    //запрос на получение недели и дат
    @POST
    @Path("/getToWeek")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getToWeek(MyTime t) {
        log.info(t.getMonday() + " " + t.getSunday() + " " + t.getWeek());
        Integer week = 0;
        LocalDate monday = LocalDate.now(ZoneId.of("Europe/Moscow"));
        LocalDate sunday = LocalDate.now(ZoneId.of("Europe/Moscow"));
        TimeWork time = new TimeWork(t.getWeek(), t.getMonday(),t.getSunday());
       // LocalDate actDay = LocalDate.now(ZoneId.of("Europe/Moscow")); //текущая дата
        LocalDate actDay = time.getSunday();
       // log.info(actDay);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if ((time.getMonday() == null) & (time.getWeek() == null) & (time.getSunday() != null)) { // получение недели и пн,вс при инициализации
            week = time.getWeek(actDay); //получение № недели
            monday = time.getMonWeek(actDay); // получение пн недели;
            sunday = monday.plusDays(6);// получение вс недели
            System.out.println("tek week " + week);

        } else if
        ((time.getMonday() == null) & (time.getWeek() == null) & (time.getSunday() == null)) { // получение недели и пн,вс при инициализации
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

            MyTime outTime = new MyTime(week, formatter.format(monday), formatter.format(sunday));
        log.info(outTime);
        return Response
                .status(Response.Status.OK)//удалена дата текущего дня
                .entity(outTime)
                .build();//выполнить;
    }

    //получение параметров группы или преподавателя
    @POST
    @Path("/getParam")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    @Consumes(value={"application/json"})
    public Response getParam(ObjSearch search) {
        log.info("получение параметров группы или преподавателя");
        log.info(search.getGrName());
        List<Search> list = em.createQuery("SELECT NEW ru.rsatu.seryakova.pojo.Search(MT.nameGroup, " +
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
                .entity(list)
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

        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i).getDayWeek() == null)) {
                list.get(i).setDayWeek(TW.getDayOfWeek(list.get(i).getDate()));
            }
        }
        return Response
                .status(Response.Status.OK)
                .entity(list)
                .build();//выполнить;

    }

    @GET//вывод информации о группах
    @Path("/getInfofGroups")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response getInfofGroups() {
        Gson gson = new Gson();
        List<InfOfGroups> groupsInfo = em.createQuery("SELECT InfGr FROM InfOfGroups InfGr ORDER BY InfGr.facultyName, " +
                "InfGr.courseNumber, InfGr.groupName", InfOfGroups.class)
                .getResultList();
        List<Faculty> faculties = new ArrayList<>();
        Faculty getFac = new Faculty();
        faculties = getFac.getFaculty(groupsInfo);

        return Response
                .status(Response.Status.OK)
                .entity(faculties)
                .build();//выполнить
    }

    //инфа о преподавателе
    @POST
    @Path("/getInfo")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response getInfo(Teachers t) {
        log.info(t);
        Gson gson = new Gson();
       // Teachers t = gson.fromJson(s,Teachers.class);
        Teachers teacher = new Teachers();
        try {
            t = em.createQuery("SELECT T FROM Teachers T where T.FIO in :fio", Teachers.class)
                    .setParameter("fio", t.getFIO())
                    .getSingleResult();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.OK)
                    .build();
        }
        return Response
                .status(Response.Status.OK)
                .entity(gson.toJson(t.getLastName() + " " + t.getName() + " " + t.getMidleName()))
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
        ExportFile pdf = new ExportFile();
        File file = pdf.createFilePdf(teacher, group, authorization, em);
        Response.ResponseBuilder fileRasp = Response.ok(file);
        fileRasp.header("Content-Disposition", "attachment; filename=DisplayName-Rasp.pdf");
        return fileRasp.build();

    }


    @GET
    @Path("/exportXsl")
    @Produces("application/pdf")
    public Response exportXsl(String s,
                              @HeaderParam("authorization") String authorization) throws IOException, DocumentException {

        ExportFile xls = new ExportFile();
        File file = xls.createFileXLS(em);
        Response.ResponseBuilder resp = Response.ok(file);
        resp.header("Content-Disposition", "attachment; filename=DisplayName-Rasp.pdf");
        return resp.build();

    }

}

