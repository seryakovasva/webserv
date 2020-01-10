package ru.rsatu.seryakova.tables;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity(name = "Teachers")//Название таблицы в БД
public class Teachers {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
 //   @Column(name = "id")
    private Long id;

 //   @Column(name = "userId")
    private Long userId;

  //  @Column(name = "MidleName")
    private String midleName;

   // @Column(name = "name")
    private String name;

   // @Column(name = "LastName")
    private String lastName;

  //  @Column(name = "FIO")
    private String FIO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMidleName() {
        return midleName;
    }

    public void setMidleName(String midleName) {
        this.midleName = midleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFIO() {
        return FIO;
    }

    public void setFIO(String FIO) {
        this.FIO = FIO;
    }
}
