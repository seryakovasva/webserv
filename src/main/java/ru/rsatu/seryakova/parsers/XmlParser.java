package ru.rsatu.seryakova.parsers;

import myClasses.*;
import ru.rsatu.seryakova.pojo.ScheduleFromFile;
import ru.rsatu.seryakova.exceptions.ParserException;
import ru.rsatu.seryakova.tables.InfOfGroups;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class XmlParser {

    public List<ScheduleFromFile> parseSchedule(String name) throws ParserException {
        List<ScheduleFromFile> schedulePair = new ArrayList<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Schedule.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            File XMLfile = new File(name);

            JAXBElement<Schedule> root = jaxbUnmarshaller.unmarshal(new StreamSource(XMLfile), Schedule.class);
            Schedule schedule = root.getValue();

            System.out.println("Расписание");
           // System.out.println(schedule.getPairs());
            for (Pair pair : schedule.getPairs()) {

                ScheduleFromFile tempSchedule = new ScheduleFromFile();
                tempSchedule.setWeek(pair.getWeek());
                tempSchedule.setDayWeek(pair.getDayOfWeek());
                tempSchedule.setNumber(String.valueOf(pair.getNumberPair()));
                System.out.println(" Пара: " + tempSchedule.getNumber());
                tempSchedule.setGroup(pair.getGroupName());
                tempSchedule.setSubGroup(pair.getSubgroup());
                tempSchedule.setDis(pair.getDiscipline());
                tempSchedule.setTeacher(pair.getTeacher());
                tempSchedule.setRoom(pair.getRoom());
                schedulePair.add(tempSchedule);
            }

        } catch (JAXBException e) {
            e.printStackTrace();
            throw new ParserException("Файл пустой или содержит ошибку в структуре");
        }
        return schedulePair;
    }

    private File getFile(String fileName) {

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file;

    }

    public List<InfOfGroups> parseInfOfGroup(String name) throws ParserException {
        System.out.println(name);
        List<InfOfGroups> groups = new ArrayList<>();

        try {
            File xmlFile = new File(name);
            //String xsdFile = "/home/sseryakova/IdeaProjects/code-with-quarkus1/src/main/resources/xsd/groups.xsd";
            JAXBContext jaxbContext = JAXBContext.newInstance(AllGroups.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

//            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//            Schema employeeSchema = sf.newSchema(getFile("groups.xsd"));
//            jaxbUnmarshaller.setSchema(employeeSchema);

            JAXBElement<AllGroups> root = jaxbUnmarshaller.unmarshal(new StreamSource(xmlFile), AllGroups.class);
            AllGroups allGroups = root.getValue();

            System.out.println("Группы" + allGroups.getFaculties().get(0).getFacultyName());

            for (Faculty f : allGroups.getFaculties()) {
                InfOfGroups infOfGroups = new InfOfGroups();
                System.out.println("  Факультет: " + f.getFacultyName());
                for (Course c : f.getCourses().getCourses()) {
                    System.out.println("      Курс: " + c.getCourseNumber());
                    for (Group g : c.getGroups().getGroups()) {
                        infOfGroups.setFacultyName(f.getFacultyName());
                        infOfGroups.setCourseNumber(c.getCourseNumber());
                        infOfGroups.setGroupName(g.getGroupName());
                        infOfGroups.setSpecialityName(g.getSpecialityName());
                        groups.add(infOfGroups);
                        infOfGroups = new InfOfGroups();
                        System.out.println("      Группа: " + g.getSpecialityName() + "  " + g.getGroupName());
                    }
                }
            }

        } catch (JAXBException  e) {
            System.out.println("DOESN'T PARSE");
            throw new ParserException("Файл пустой или содержит ошибку в структуре");
        }
//        catch (SAXException e) {
//            e.printStackTrace();
//        }
        return groups;
    }

    public void exportToBD(List<InfOfGroups> groups, EntityManager em) {


        em.createQuery("delete from InfOfGroups").executeUpdate();

        for (InfOfGroups group : groups) {
            System.out.println(group.getGroupName());
            em.persist(group);
        }
    }
}
