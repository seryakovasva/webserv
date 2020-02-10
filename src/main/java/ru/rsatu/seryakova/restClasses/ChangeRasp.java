package ru.rsatu.seryakova.restClasses;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import ru.rsatu.seryakova.pojo.*;
import ru.rsatu.seryakova.utilites.ForAuth;
import ru.rsatu.seryakova.utilites.TimeWork;
import ru.rsatu.seryakova.utilites.Util;
import ru.rsatu.seryakova.exceptions.ParserException;
import ru.rsatu.seryakova.parsers.XlsParser;
import ru.rsatu.seryakova.parsers.XmlParser;
import ru.rsatu.seryakova.tables.InfOfGroups;
import ru.rsatu.seryakova.tables.Shedule;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Stateless
@Path("/changeRasp")
public class ChangeRasp {

    @Inject
    private EntityManager em;

    private static final Logger log = Logger.getLogger(ChangeRasp.class);
    //Папка для скаченных файлов
    private final String UPLOADED_FILE_PATH =
            "/home/sseryakova/IdeaProjects/code-with-quarkus1/src/main/resources/";

    @Transactional
    @POST
    @Path("/addInfofGroups")
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addInfofGroups(MultipartFormDataInput input, @HeaderParam("authorization") String authorization) {
        Gson g = new Gson();
        ForAuth forAuth = new ForAuth();
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))) {
            Util utils = new Util();
            String fileName = "";
            Map<String, List<InputPart>> formParts = input.getFormDataMap();
            List<InputPart> inPart = formParts.get("groups");
            for (InputPart inputPart : inPart) {
                try {
                    MultivaluedMap<String, String> headers = inputPart.getHeaders();
                    fileName = utils.parseFileName(headers);
                    InputStream istream = inputPart.getBody(InputStream.class, null);
                    fileName = UPLOADED_FILE_PATH + fileName;
                    utils.saveFile(istream, fileName);
                    String output = "File saved to server location : " + fileName;
                    System.out.println(output);
                    XmlParser parser = new XmlParser();
                    List<InfOfGroups> groups = new ArrayList<>();
                    try {
                        groups = parser.parseInfOfGroup(fileName);
                        parser.exportToBD(groups, em);
                    } catch (ParserException e) {
                        log.error(e.getMessage());
                        // e.printStackTrace();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(g.toJson(e.getMessage()))
                                .build();
                    }

                } catch (IOException e) {
                    log.error(e.getMessage());
                    //e.printStackTrace();
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(g.toJson("Ошибка в расписании"))
                            .build();
                }

            }

            return Response.status(Response.Status.OK)
                    .entity(g.toJson("Информация загружена"))
                    .build();

        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(g.toJson("Войдите в учетную запись"))
                    .build();
        }
    }


    @Transactional
    @POST
    @Path("/inputFile")
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inputFile (MultipartFormDataInput input, @HeaderParam("authorization") String authorization) {
        Gson g = new Gson();
        ForAuth forAuth = new ForAuth();
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))) {
            Util utils = new Util();
            String fileName = "";
            String sem = "";

            Map<String, List<InputPart>> formParts = input.getFormDataMap();
            if (formParts.get("spring") == null) {
                sem = "autumn";
            } else {
                sem = "spring";
            }
            List<InputPart> inPart = formParts.get(sem);
            for (InputPart inputPart : inPart) {
                try {
                    MultivaluedMap<String, String> headers = inputPart.getHeaders();
                    fileName = utils.parseFileName(headers);
                    InputStream istream = inputPart.getBody(InputStream.class, null);
                    fileName = UPLOADED_FILE_PATH + fileName;
                    utils.saveFile(istream, fileName);
                    String output = "File saved to server location : " + fileName;
                    //File inputXls = new File(fileName);
                    //log.info(inputXls);
                    log.info(fileName.substring(fileName.indexOf('.') + 1, fileName.length()));
                    try {
                        if (fileName.substring(fileName.indexOf('.') + 1, fileName.length()).equals("xls")) {
                            XlsParser parser = new XlsParser();
                            List<ScheduleFromFile> schedule = new ArrayList<>();

                            schedule = parser.parse(fileName);
                            parser.exportToBD(schedule, sem, em);
                        } else {
                            XmlParser parser = new XmlParser();
                            List<ScheduleFromFile> schedule = new ArrayList<>();
                            schedule = parser.parseSchedule(fileName);
                            log.info(schedule.size());
                            XlsParser parser1 = new XlsParser();
                            parser1.exportToBD(schedule, sem, em);
                        }
                    } catch (ParserException e) {
                        log.error(e.getMessage());
                        // e.printStackTrace();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(g.toJson(e.getMessage()))
                                .build();
                    }


                } catch (IOException e) {
                    log.error(e.getMessage());
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(g.toJson("Ошибка в расписании"))
                            .build();
                }
            }
            return Response.status(Response.Status.OK)
                    .entity(g.toJson("Информация загружена"))
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(g.toJson("Войдите в учетную запись"))
                    .build();
        }
    }


    //перенос пары
    @Transactional
    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response transfer (String s, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        log.info("Перенос пары" + forAuth.parseToken(authorization, em).getRole());
        System.out.println(s);
        Gson gson =new Gson();
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))
                || (forAuth.parseToken(authorization, em).getRole().equals("teacher"))) {
            Transfer transfer = gson.fromJson(s, Transfer.class);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            Boolean stop = true;
            //проверяем свободна ли аудитория в это время или нет

            String inform = em.createQuery("SELECT  (MT.info) FROM Shedule MT WHERE MT.id_el in :id ", String.class)
                    .setParameter("id", transfer.getId_el())
                    .getSingleResult();
            log.info(inform);

            Long auditor = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.room like :r and " +
                    "MT.date in :day and MT.numberPair in :number", Long.class)
                    .setParameter("r", transfer.getRoom())
                    .setParameter("day", formatter.format(transfer.getDate()))
                    .setParameter("number", transfer.getNumberPar())
                    .getSingleResult();

            Long k = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.teacher IN :T and " +
                    "MT.date in :day and MT.numberPair in :number", Long.class)
                    .setParameter("T", transfer.getTeacher())
                    .setParameter("day", formatter.format(transfer.getDate()))
                    .setParameter("number", transfer.getNumberPar())
                    .getSingleResult();

            if ((inform.equals("Занятие отменено")) && (auditor == 0)) {
                Shedule pair = em.createQuery("SELECT MT FROM Shedule MT WHERE MT.id_el in :id", Shedule.class)
                        .setParameter("id", transfer.getId_el()
                        )
                        .getSingleResult();
                pair.setInfo("Занятие перенесено на " + formatter.format(transfer.getDate()) + " на " + transfer.getNumberPar() + " пару," + "\n" + " аудитория "
                        + transfer.getRoom());

                Shedule newPair = new Shedule();
                TimeWork TW = new TimeWork();
                newPair.setNameGroup(pair.getNameGroup());
                newPair.setDate(formatter.format(transfer.getDate()));
                newPair.setDiscipline(pair.getDiscipline());
                newPair.setTeacher(pair.getTeacher());
                newPair.setTypepair(pair.getTypepair());
                newPair.setNumberPair(transfer.getNumberPar());
                newPair.setRoom(transfer.getRoom());
                newPair.setDayWeek(TW.getDayOfWeek(newPair.getDate()));
                em.merge(newPair);
                return Response
                        .status(Response.Status.OK)
                        .entity(gson.toJson("Занятие перенесено"))
                        .build();
            }
            //аудитория и препод заняты
            else if ((k != 0) && (auditor != 0)) {
                Shedule pair = em.createQuery("SELECT MT FROM Shedule MT WHERE MT.id_el in :id", Shedule.class)
                        .setParameter("id", transfer.getId_el()
                        )
                        .getSingleResult();
                //возможно ли совместное проведение занятия?
                k = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.teacher IN :T and " +
                        "MT.date in :day and MT.numberPair in :number and MT.discipline  like :dis and " +
                        "MT.room like :r and MT.typepair like :typeP", Long.class)
                        .setParameter("T", pair.getTeacher())
                        .setParameter("day", pair.getDate())
                        .setParameter("number", pair.getNumberPair())
                        .setParameter("dis", pair.getDiscipline())
                        .setParameter("typeP", pair.getTypepair())
                        .setParameter("r", pair.getRoom())
                        .getSingleResult();
                if (k != 0) {
                    pair.setInfo("Занятие перенесено на " + formatter.format(transfer.getDate()) + " на " + transfer.getNumberPar() + " пару," + "\n" + " аудитория "
                            + transfer.getRoom());

                    Shedule newPair = new Shedule();
                    TimeWork TW = new TimeWork();
                    newPair.setNameGroup(pair.getNameGroup());
                    newPair.setDate(formatter.format(transfer.getDate()));
                    newPair.setDiscipline(pair.getDiscipline());
                    newPair.setTeacher(pair.getTeacher());
                    newPair.setTypepair(pair.getTypepair());
                    newPair.setNumberPair(transfer.getNumberPar());
                    newPair.setRoom(transfer.getRoom());
                    newPair.setDayWeek(TW.getDayOfWeek(newPair.getDate()));
                    em.merge(newPair);
                    return Response
                            .status(Response.Status.OK)
                            .entity(gson.toJson("Занятие перенесено"))
                            .build();
                }
            } else if ((k == 0) && (auditor == 0)) {
                Shedule pair = em.createQuery("SELECT MT FROM Shedule MT WHERE MT.id_el in :id", Shedule.class)
                        .setParameter("id", transfer.getId_el()
                        )
                        .getSingleResult();
                System.out.println("para " + pair);
                pair.setInfo("Занятие перенесено на " + formatter.format(transfer.getDate()) + " на " + transfer.getNumberPar() + " пару," + "\n" + " аудитория "
                        + transfer.getRoom());

                Shedule newPair = new Shedule();
                TimeWork TW = new TimeWork();
                newPair.setNameGroup(pair.getNameGroup());
                newPair.setDate(formatter.format(transfer.getDate()));
                newPair.setDiscipline(pair.getDiscipline());
                newPair.setTeacher(pair.getTeacher());
                newPair.setTypepair(pair.getTypepair());
                newPair.setNumberPair(transfer.getNumberPar());
                newPair.setRoom(transfer.getRoom());
                newPair.setDayWeek(TW.getDayOfWeek(newPair.getDate()));
                em.merge(newPair);
                return Response
                        .status(Response.Status.OK)
                        .entity(gson.toJson("Занятие перенесено"))
                        .build();
            } else {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(gson.toJson("Выберите другое время или аудиторию"))
                        .build();
            }
        }
        return Response
                .status(Response.Status.OK)
                .entity(gson.toJson("Войдите в аккаунт"))
                .build();
    }

    //удаление занятия
    @Transactional
    @POST
    @Path("/deletePair")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response deletePair (String s, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        log.info("Удаление пары");
        log.info(s);
        Gson gson =new Gson();
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))) {
            log.info(s);
            Shedule pair = gson.fromJson(s, Shedule.class);
            try {
                em.createQuery("DELETE from Shedule MT WHERE MT.id_el = :id")
                        .setParameter("id",pair.getId_el())
                        .executeUpdate();
                return Response
                        .status(Response.Status.OK)
                        .entity(gson.toJson("Занятие удалено"))
                        .build();
            } catch (Exception ex) {
                log.error("Ошибка при удалении занятия");
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(gson.toJson("Ошибка при удалении занятия"))
                        .build();
            }
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(gson.toJson("Войдите в аккаунт"))
                .build();
    }

    //поиск пар авторизованного пользователя
    @Transactional
    @POST
    @Path("/raspTeach")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(MediaType.APPLICATION_JSON)
    //@Consumes(value={"application/json"})
    public Response raspTeach (String s, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
//        forAuth.parseToken(authorization);
        log.info("поиск пар авторизованного пользователя");
        System.out.println(s);
        Gson gson =new Gson();
        if (forAuth.parseToken(authorization, em) != null) {

            String name = "";
            ObjSearch search = gson.fromJson(s, ObjSearch.class);
            if (forAuth.parseToken(authorization, em).getRole().equals("teacher")) {
                name = em.createQuery("SELECT T.FIO FROM Teachers T left join  Users U on T.userId = U.userId " +
                        "WHERE U.login like :login", String.class)
                        .setParameter("login", forAuth.parseToken(authorization, em).getLogin())
                        .getSingleResult();
            } else if ((forAuth.parseToken(authorization, em).getRole().equals("admin")) ||
                    (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))) {
                name = search.getTeacher();
            }

            TimeWork TW = gson.fromJson(s, TimeWork.class);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            if (search.getGrName() != null) {
                if (search.getGrName().size() == 0) {
                    search.setGrName(null);
                }
            }
            if (search.getDiscipline() != null) {
                if (search.getDiscipline().size() == 0) {
                    search.setDiscipline(null);
                }
            }
            log.info(name + " name");
            List<Shedule> list = new ArrayList<>();
            if (!((search.getDate() == null) && (search.getDiscipline() == null) && (search.getGrName() == null) &&
                    (name == null))) {
                log.info(" pusto");
                log.info(search.getDiscipline());
                list = em.createQuery("SELECT MT FROM Shedule MT WHERE MT.nameGroup IN :NG and " +
                        "MT.teacher IN :T and MT.discipline IN :Dis and MT.date in :day" +
                        " ORDER BY CAST(MT.date as date), MT.numberPair ", Shedule.class)
                        .setParameter("day", search.getDate() != null
                                ? formatter.format(search.getDate())
                                : TW.getDayWeek(TW.getMonday())
                        )
                        .setParameter("NG", search.getGrName() != null
                                ? search.getGrName()
                                : em.createQuery("SELECT DISTINCT MT.nameGroup FROM Shedule MT").getResultList()
                        )//имя группы
                        .setParameter("T", name != null
                                ? name
                                : em.createQuery("SELECT DISTINCT MT.teacher FROM Shedule MT").getResultList()
                        )//преподователь
                        .setParameter("Dis", search.getDiscipline() != null ?
                                search.getDiscipline() :
                                em.createQuery("SELECT DISTINCT MT.discipline FROM Shedule MT").getResultList()
                        )//дисциплина
                        .getResultList();

                log.info(list.size());

                for (int i = 0; i < list.size(); i++) {
                    if ((list.get(i).getDayWeek() == null)) {
                        list.get(i).setDayWeek(TW.getDayOfWeek(list.get(i).getDate()));
                    }
                }
                return Response
                        .status(Response.Status.OK)
                        .entity(gson.toJson(list))
                        .build();
            }
            log.info("tut");
            return Response
                    .status(Response.Status.OK)
                    .entity(gson.toJson(0))
                    .build();
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(gson.toJson("Войдите в аккаунт"))
                .build();
    }

    @Transactional
    @GET
    @Path("/getParamPair")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response getParamPair(@HeaderParam("authorization") String authorization){
        ForAuth forAuth = new ForAuth();
        Gson gson =new Gson();
        if (forAuth.parseToken(authorization, em) != null) {
            String name = "";
//            log.info(s);
            if (forAuth.parseToken(authorization, em).getRole().equals("teacher")) {
                name = em.createQuery("SELECT T.FIO FROM Teachers T left join  Users U on T.userId = U.userId " +
                        "WHERE U.login like :login", String.class)
                        .setParameter("login", forAuth.parseToken(authorization, em).getLogin())
                        .getSingleResult();
            }

            List list = em.createQuery("SELECT NEW ru.rsatu.seryakova.pojo.Search(MT.nameGroup, " +
                    "MT.room, MT.teacher, MT.discipline) FROM Shedule MT left join Teachers T on T.FIO = MT.teacher " +
                    "WHERE T.FIO like : name", Search.class)
                    .setParameter("name", name).getResultList();
            return Response
                    .status(Response.Status.OK)
                    .entity(gson.toJson(list))
                    .build();//выполнить
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(gson.toJson("Войдите в аккаунт"))
                .build();
    }

    //получение дисциплин
    @Transactional
    @POST
    @Path("/getDis")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response getDisGroup (String s, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        Gson gson =new Gson();
        if (forAuth.parseToken(authorization, em) != null) {
            ObjSearch search = gson.fromJson(s, ObjSearch.class);
            String name = null;
            //получение фамилии преподавателя
            log.info("получение фамилии преподавателя");
            if (forAuth.parseToken(authorization, em).getRole().equals("teacher")) {
                name = em.createQuery("SELECT T.FIO FROM Teachers T left join  Users U on T.userId = U.userId " +
                        "WHERE U.login like :login", String.class)
                        .setParameter("login", forAuth.parseToken(authorization, em).getLogin())
                        .getSingleResult();
            }
            else if ((forAuth.parseToken(authorization, em).getRole().equals("admin")) ||
                    (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))) {
                name = search.getTeacher();
                log.info(name);
            }

            List<String> dis = em.createQuery("SELECT  (MT.discipline) FROM Shedule MT WHERE MT.nameGroup IN :NG and " +
                    "MT.teacher in :teach", String.class)
                    .setParameter("NG",search.getGroup() != null
                            ? search.getGroup()
                            : em.createQuery("SELECT DISTINCT MT.nameGroup FROM Shedule MT").getResultList())
                    .setParameter("teach", name != null
                            ? name
                            : em.createQuery("SELECT DISTINCT MT.teacher FROM Shedule MT").getResultList())
                    .getResultList();

            return Response
                    .status(Response.Status.OK)
                    .entity(dis)
                    .build();//выполнить

        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson("Войдите в аккаунт"))
                    .build();
        }
    }

    String getTypePair(String type) {
        String sokrType;
        if (type.equals("Лекция")) {
            sokrType = "л";

        } else if (type.equals("Практика")) {
            sokrType = "пр";
        } else {
            sokrType = "л/р";
        }
        return sokrType;
    }

    //добавление пары
    @Transactional
    @POST
    @Path("/addPair")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response addPair (String s, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        log.info("Добавление пары");
        log.info(s);
        Gson gson =new Gson();
        Shedule MT = gson.fromJson(s, Shedule.class);
        if (forAuth.parseToken(authorization, em).getRole().equals("teacher")) {
            String name = "";
            if (MT.getSubGroup().equals("все")) {
                MT.setSubGroup("");
            }
            name = em.createQuery("SELECT T.FIO FROM Teachers T left join  Users U on T.userId = U.userId " +
                    "WHERE U.login like :login", String.class)
                    .setParameter("login", forAuth.parseToken(authorization, em).getLogin())
                    .getSingleResult();
            MT.setTeacher(name);
        }
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))
                ||(forAuth.parseToken(authorization, em).getRole().equals("teacher"))) {
            if (MT.getSubGroup().equals("все")) {
                MT.setSubGroup("");
            }
            if (!MT.getTypepair().equals("к")) {
                MT.setTypepair(getTypePair(MT.getTypepair()));
            }
            log.info(MT.getTeacher() + " " + MT.getDiscipline() + " " + MT.getTypepair());
            Long k = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.teacher IN :T and " +
                    "MT.date in :day and MT.numberPair in :number", Long.class)
                    .setParameter("T", MT.getTeacher())
                    .setParameter("day", MT.getDate())
                    .setParameter("number", MT.getNumberPair())
                    .getSingleResult();

            Long auditor = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.room like :r and " +
                    "MT.date in :day and MT.numberPair in :number", Long.class)
                    .setParameter("r", MT.getRoom())
                    .setParameter("day", MT.getDate())
                    .setParameter("number", MT.getNumberPair())
                    .getSingleResult();
            //аудитория и препод заняты
            if ((k != 0) && (auditor != 0)) {
                //возможно ли совместное проведение занятия?
                k = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.teacher IN :T and " +
                        "MT.date in :day and MT.numberPair in :number and MT.discipline  like :dis and " +
                        "MT.room like :r and MT.typepair like :typeP", Long.class)
                        .setParameter("T", MT.getTeacher())
                        .setParameter("day", MT.getDate())
                        .setParameter("number", MT.getNumberPair())
                        .setParameter("dis", MT.getDiscipline())
                        .setParameter("typeP", MT.getTypepair())
                        .setParameter("r", MT.getRoom())
                        .getSingleResult();
                //группа свободна?
                Long k1 = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.nameGroup IN :G and " +
                        "MT.date in :day and MT.numberPair in :number", Long.class)
                        .setParameter("G", MT.getNameGroup())
                        .setParameter("day", MT.getDate())
                        .setParameter("number", MT.getNumberPair())
                        .getSingleResult();
                log.info(k);
                if ((k1 == 0) && (k != 0)) {
                    TimeWork TW = new TimeWork();
                    MT.setDayWeek(TW.getDayOfWeek(MT.getDate()));
                    em.merge(MT);
                    return Response
                            .status(Response.Status.OK)
                            .entity(gson.toJson("Добавлено"))
                            .build();//выполнить
                } else {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(gson.toJson("Выберите другое время или аудиторию"))
                            .build();
                }
            } else if ((k == 0) && (auditor == 0)) {
                //группа свободна?
                Long k1 = em.createQuery("SELECT count (MT.discipline) FROM Shedule MT WHERE MT.nameGroup IN :G and " +
                        "MT.date in :day and MT.numberPair in :number", Long.class)
                        .setParameter("G", MT.getNameGroup())
                        .setParameter("day", MT.getDate())
                        .setParameter("number", MT.getNumberPair())
                        .getSingleResult();
//                //есть ли в это время пара у препода
                if (k1 == 0) {
                    TimeWork TW = new TimeWork();
                    MT.setDayWeek(TW.getDayOfWeek(MT.getDate()));
                    em.merge(MT);
                    return Response
                            .status(Response.Status.OK)
                            .entity(gson.toJson("Добавлено"))
                            .build();//выполнить
                }
            }
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson("Выберите другое время или аудиторию"))
                    .build();
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson("Выберите другое время или аудиторию"))
                    .build();
        }
    }

    //свободные пары группы
    @Transactional
    @POST
    @Path("/getPair")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response getPair (ObjSearch search, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        Gson gson = new Gson();
        //ObjSearch search = gson.fromJson(s, ObjSearch.class);

        String respMsg = "Ok";
        log.info("Свободное пары");
        if (search.getSubgroup().equals("все")) {
            search.setSubgroup("");
        }
        log.info(search.getGroup() + search.getDate() + search.getSubgroup());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))
                || (forAuth.parseToken(authorization, em).getRole().equals("teacher"))) { //проверка на авторизацию

            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow")); //текущая дата
            if ((search.getDate().compareTo(today) == 0) || (today.compareTo(search.getDate()) < 0)) { //если новая дата выбрана текущим днем или позже
                List<Integer> number = em.createQuery("SELECT  (MT.numberPair) FROM Shedule MT WHERE MT.nameGroup IN :NG and " +
                        "MT.date in :day and MT.subgroup in (:sg,'')", Integer.class)
                        .setParameter("NG", search.getGroup())
                        .setParameter("day", formatter.format(search.getDate()))
                        .setParameter("sg", !search.getSubgroup().equals("")
                                ? search.getSubgroup()
                                : em.createQuery("SELECT DISTINCT MT.subgroup FROM Shedule MT").getResultList()
                        )//
                        .getResultList();
                System.out.println("num pair" + number);
                ArrayList<Integer> allNum = new ArrayList<Integer>();
                for (int i = 0; i < 7; i++) {
                    allNum.add(i);
                }

                for (int i = 0; i < number.size(); i++) {
                    for (int k = 0; k < allNum.size(); k++) {

                        if (number.get(i).equals(allNum.get(k))) {
                            allNum.remove(k); //удаление повторяющейся пары
                        }
                    }
                }
                if (allNum.size() == 0) {
                    respMsg="Свободных пар не обнаружено";
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(gson.toJson(respMsg))
                            .build();
                } else {
                    return Response
                            .status(Response.Status.OK)
                            .entity(gson.toJson(allNum))
                            .build();//выполнить
                }
            } else {
                respMsg="Выберите другую дату";
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(respMsg)
                        .build();
            }


        } else {
            respMsg = "Войдите в аккаунт";
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(respMsg))
                    .build();
        }
    }


    //отмена пары
    @Transactional
    @POST
    @Path("/cancelPair")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response cancelPair (Shedule mt, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        Gson gson =new Gson();
        log.info("Отмена пары");
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))
                || (forAuth.parseToken(authorization, em).getRole().equals("teacher"))) {
            //System.out.println(s);
            //Shedule mt = gson.fromJson(s, Shedule.class);
            Shedule pair = em.createQuery("SELECT  MT FROM Shedule MT WHERE MT.id_el IN :id ", Shedule.class)
                    .setParameter("id",mt.getId_el())
                    .getSingleResult();
            pair.setInfo("Занятие отменено");
            em.merge(pair);
            System.out.println("отмена пары " + mt);
            return Response
                    .status(Response.Status.OK)
                    .entity(gson.toJson(mt))
                    .build();//выполнить
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson("Войдите в аккаунт"))
                    .build();
        }
    }

    //----------восстановление пары
    @Transactional
    @POST
    @Path("/recovery")
    @Produces(MediaType.APPLICATION_JSON) //тип, возвращаемый ресурсом
    @Consumes(value={"application/json"})
    public Response recovery (Shedule mt, @HeaderParam("authorization") String authorization) {
        ForAuth forAuth = new ForAuth();
        log.info("Восстановление пары");
        //log.info(s);
        Gson gson =new Gson();
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))
                || (forAuth.parseToken(authorization, em).getRole().equals("teacher"))) {
            //Shedule mt = gson.fromJson(s, Shedule.class);
            Shedule pair = em.createQuery("SELECT  MT FROM Shedule MT WHERE MT.id_el IN :id ", Shedule.class)
                    .setParameter("id",mt.getId_el())
                    .getSingleResult();
            if (pair.getInfo().equals("Занятие отменено")) {
                pair.setInfo(null);
                em.merge(pair);
            } else {
                String date = pair.getInfo().substring(22, 32);
                String number = pair.getInfo().substring(36, 37);
                String auditoria = pair.getInfo().substring(55,mt.getInfo().length());
                log.info(auditoria);
                pair.setInfo(null);
                em.merge(pair);
                mt.setDate(date);
                Shedule pair1 = em.createQuery("SELECT MT FROM Shedule MT WHERE MT.nameGroup IN :NG and " +
                        "MT.date in :day and MT.numberPair in :number and MT.room IN : audit", Shedule.class)
                        .setParameter("NG", mt.getNameGroup())
                        .setParameter("day", mt.getDate())
                        .setParameter("number", Integer.parseInt(number))
                        .setParameter("audit", auditoria)
                        .getSingleResult();
                em.remove(pair1);
                log.info(date + " ," + number);
            }


            return Response
                    .status(Response.Status.OK)
                    .entity(gson.toJson("Занятие восстановлено"))
                    .build();//выполнить
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(gson.toJson("Войдите в аккаунт"))
                .build();
    }
}
