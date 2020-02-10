package ru.rsatu.seryakova.pojo;

public class AuthError {
    private String login;
    private String hashpassword;
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHashpassword() {
        return hashpassword;
    }

    public void setHashpassword(String password) {
        this.hashpassword = password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

}
