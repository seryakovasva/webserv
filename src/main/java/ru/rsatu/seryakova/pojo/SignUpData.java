package ru.rsatu.seryakova.pojo;

public class SignUpData {

    private String login;
    private String lastName;
    private String midleName;
    private String name;
    private String role;
    private String hashPassword;


    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }


    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMidlename() {
        return midleName;
    }

    public void setMidlename(String midlename) {
        midleName = midlename;
    }

    public String getName() {
        return name;
    }

}
