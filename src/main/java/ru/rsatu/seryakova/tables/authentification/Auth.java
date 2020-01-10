package ru.rsatu.seryakova.tables.authentification;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

@XmlRootElement
@Entity(name = "Auth")//Название таблицы в БД
public class Auth {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
  //  @Column(name = "authId")
    private Long authId;

  //  @Column(name = "userId")
    private Long userId;

  //  @Column(name = "hashPassword")
    private String hashPassword;

 //   @Column(name = "secretKey")
    private String secretKey;

 //   @Column(name = "accessToken")
    private String accessToken;

 //   @Column(name = "refreshToken")
    private String refreshToken;

 //   @Column(name = "timeToKillToken")
    private Timestamp timeToKillToken;

    public Timestamp getTimeToKillToken() {
        return timeToKillToken;
    }

    public void setTimeToKillToken(Timestamp timeToKillToken) {
        this.timeToKillToken = timeToKillToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String sekretKey) {
        this.secretKey = sekretKey;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "authId=" + authId +
                ", userId=" + userId +
                ", hashPassword='" + hashPassword + '\'' +
                '}';
    }
}
