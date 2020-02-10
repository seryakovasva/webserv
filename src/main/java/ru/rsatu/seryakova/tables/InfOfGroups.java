package ru.rsatu.seryakova.tables;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity(name = "InfOfGroups")//Название таблицы в БД
public class InfOfGroups {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    //@Column(name = "id")
    private Long id;

   // @Column(name = "groupName")
    private String groupName;

   // @Column(name = "specialityName")
    private String specialityName;

    //@Column(name = "courseNumber")
    private int courseNumber;

   // @Column(name = "facultyName")
    private String facultyName;


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSpecialityName() {
        return specialityName;
    }

    public void setSpecialityName(String specialityName) {
        this.specialityName = specialityName;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }
}

