package ru.rsatu.seryakova.tables.authentification;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity(name = "Users")//Название таблицы в БД
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long userId;

//    @Column(name = "login")
    private String login;

  //  @Column(name = "name")
    private String name;

  //  @Column(name = "LastName")
    private String LastName;

  //  @Column(name = "MidleName")
    private String MidleName;

  //  @Column(name = "role")
    private String role;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getMidleName() {
        return MidleName;
    }

    public void setMidleName(String midleName) {
        MidleName = midleName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
