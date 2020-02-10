package ru.rsatu.seryakova.restClasses;

import com.google.gson.Gson;
import io.jsonwebtoken.JwtException;
import org.apache.log4j.Logger;
import ru.rsatu.seryakova.pojo.AuthError;
import ru.rsatu.seryakova.pojo.SignInData;
import ru.rsatu.seryakova.pojo.SignUpData;
import ru.rsatu.seryakova.utilites.ForAuth;
import ru.rsatu.seryakova.tables.Teachers;
import ru.rsatu.seryakova.tables.authentification.Auth;
import ru.rsatu.seryakova.tables.authentification.Users;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.Date;

@Stateless
@Path("/auth")
public class Authentification {
    @Inject
    EntityManager em;
    private static final Logger log = Logger.getLogger(Authentification.class);

    //
//    @Transactional
//    @POST
//    @Path("/signIn")
//    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
//    public Response signIn(String s) {
//        System.out.println("sign in");
//        log.info(s);
//        Gson g = new Gson();
//        String respMsg = "Ok";
//        AuthError authError = new AuthError();
//        SignInData signInData = g.fromJson(s, SignInData.class);
//        log.info("Аунтетификация");
//        log.info("Логин: " + signInData.getLogin());
//        log.info("Пароль: " + signInData.getHashPassword());
//
//        Long kol = em.createQuery("SELECT COUNT (U.login) FROM Users U WHERE U.login like :login ", Long.class)
//                .setParameter("login", signInData.getLogin())
//                .getSingleResult();
//        if (kol == 0) {
//            respMsg = "Неправильный логин";
//            System.out.println("логин");
//            authError.setLogin(respMsg);
//            return Response
//                    .status(Response.Status.BAD_REQUEST)
//                    .entity(g.toJson(authError))
//                    .build();
//        } else {
//
//            Long kol11 = em.createQuery("SELECT COUNT (U.login) FROM Users U left join Auth A on A.userId = U.userId " +
//                    "WHERE U.login like :login and A.hashPassword like :hash", Long.class)
//                    .setParameter("login", signInData.getLogin())
//                    .setParameter("hash", signInData.getHashPassword())
//                    .getSingleResult();
//
//            if (kol11 == 0) {
//                respMsg = "Неправильный пароль";
//                System.out.println("пароль");
//                authError.setPassword(respMsg);
//                return Response
//                        .status(Response.Status.BAD_REQUEST)
//                        .entity(g.toJson(authError))
//                        .build();
//
//
//            } else {
//                ForAuth token = new ForAuth();
//                String role = em.createQuery("SELECT U.role FROM Users U WHERE U.login = :log", String.class)
//                        .setParameter("log", signInData.getLogin())
//                        .getSingleResult();
//
//                Auth auth = em.createQuery("SELECT  A FROM Users U left join Auth A on A.userId = U.userId " +
//                        "WHERE U.login like :login and A.hashPassword like :hash", Auth.class)
//                        .setParameter("login", signInData.getLogin())
//                        .setParameter("hash", signInData.getHashPassword())
//                        .getSingleResult();
//
//                String secretKey = token.generateKey(15);
//                auth.setSecretKey(secretKey);
//                Timestamp timestamp = new Timestamp(System.currentTimeMillis() +  8* 60 * 2000);
//                auth.setTimeToKillToken(timestamp);
//                String tok = token.createTok(signInData.getLogin(), role, secretKey, timestamp);
//                auth.setAccessToken(tok);
//
//                auth.setRefreshToken(token.generateKey(10));
//                em.merge(auth);
//                Gson gson = new Gson();
//
//                return Response
//                        .status(Response.Status.OK)
//                        .entity(gson.toJson(new ForAuth(tok, auth.getRefreshToken())))
//                        .build();
//            }
//        }
//    }

    @Transactional
    @POST
    @Path("/signIn")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response signIn(SignInData signInData) {
        System.out.println("sign in");
       // log.info(s);
        Gson g = new Gson();
        String respMsg = "Ok";
        AuthError authError = new AuthError();
       // SignInData signInData = g.fromJson(s, SignInData.class);
        log.info("Аунтетификация");
        log.info("Логин: " + signInData.getLogin());
        log.info("Пароль: " + signInData.getHashPassword());

        Auth auth = em.createQuery("SELECT  A FROM Users U left join Auth A on A.userId = U.userId " +
                "WHERE U.login like :login", Auth.class)
                .setParameter("login", signInData.getLogin())
                .getSingleResult();

        if (auth == null) {
            respMsg = "Неправильный логин";
            System.out.println("логин");
            authError.setLogin(respMsg);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(g.toJson(authError))
                    .build();
        } else {
            if (!(auth.getHashPassword().equals(signInData.getHashPassword()))) {
                respMsg = "Неправильный пароль";
                System.out.println("пароль");
                authError.setPassword(respMsg);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(g.toJson(authError))
                        .build();


            } else {
                ForAuth token = new ForAuth();
                String role = em.createQuery("SELECT U.role FROM Users U WHERE U.login = :log", String.class)
                        .setParameter("log", signInData.getLogin())
                        .getSingleResult();
                String secretKey = token.generateKey(15);
                auth.setSecretKey(secretKey);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis() +  8* 60 * 2000);
                auth.setTimeToKillToken(timestamp);
                String tok = token.createTok(signInData.getLogin(), role, secretKey, timestamp);
                auth.setAccessToken(tok);

                auth.setRefreshToken(token.generateKey(10));
                em.merge(auth);
                Gson gson = new Gson();
                ForAuth outAuth = new ForAuth(tok, auth.getRefreshToken());

                return Response
                        .status(Response.Status.OK)
                        .entity(outAuth)
                        .build();
            }
        }
    }

    @Transactional
    @POST
    @Path("/signUp")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response signUp(SignUpData signUpData, @HeaderParam("authorization") String authorization) {
        AuthError authError = new AuthError();
        String respMsg = "Ok";
        Gson g = new Gson();
        log.info("Регистрация");
        ForAuth forAuth = new ForAuth();
        if ((forAuth.parseToken(authorization, em).getRole().equals("admin"))
                || (forAuth.parseToken(authorization, em).getRole().equals("scheduleEditor"))) {
            //SignUpData signUpData = g.fromJson(s, SignUpData.class);
            Users user = new Users();
            user.setLastName(signUpData.getLastName());
            user.setLogin(signUpData.getLogin());
            user.setMidleName(signUpData.getMidlename());
            user.setName(signUpData.getName());
            user.setRole(signUpData.getRole());
            Auth auth = new Auth();
            auth.setHashPassword(signUpData.getHashPassword());
            //существует ли такой пользователь?
            Long kol = em.createQuery("SELECT COUNT (U.login) from Users U where U.login = :login", Long.class)
                    .setParameter("login", user.getLogin())
                    .getSingleResult();
            if (kol == 0) {
                //добавление user
                em.merge(user);
                try {//получение user id
                    Long userId = em.createQuery("SELECT U.userId from Users U where U.login = :login", Long.class)
                            .setParameter("login", user.getLogin())
                            .getSingleResult();
                    auth.setUserId(userId);
                    log.info(userId);
                } catch (NoResultException e1) {
                    e1.printStackTrace();
                    respMsg = "Ошибка при регистрации";
                    log.error("Ошибка при регистрации");
                }
                log.info(auth.toString());
                em.merge(auth);//добавление в бд
                respMsg = "Пользователь зарегистрирован";
                if (user.getRole().equals("teacher")) {
                    Teachers teacher = new Teachers();
                    teacher.setLastName(user.getLastName());
                    teacher.setName(user.getName());
                    teacher.setMidleName(user.getMidleName());
                    String fio = user.getLastName() + " " + user.getName().substring(0, 1) + "." + user.getMidleName().substring(0, 1) + ".";
                    teacher.setFIO(fio);
                    teacher.setUserId(auth.getUserId());
                    em.merge(teacher);
                }

                return Response
                        .status(Response.Status.OK)
                        .entity(g.toJson(respMsg))
                        .build();//выполнить

            } else {
                respMsg = "Пользователь с таким логином уже существует";
                log.error("Пользователь с таким логином уже существует");
                authError.setLogin(respMsg);
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(authError)
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(g.toJson("Войдите в учетную запись"))
                    .build();
        }
    }

    @Transactional
    @GET
    @Path("/updateTok")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response updateTok( @HeaderParam("authorization") String authorization) {
        log.info("Обновление токена");
        Gson g = new Gson();
        ForAuth token = new ForAuth();
        log.info(authorization.substring(7, authorization.length()));
        Long k = em.createQuery("SELECT  count (A.userId) FROM  Auth A " +
                "WHERE A.refreshToken like :tok", Long.class)
                .setParameter("tok", authorization.substring(7, authorization.length()))
                .getSingleResult();
        if (k != 0) {
            log.info("k " + k);
            Users user = em.createQuery("SELECT U FROM Users U left join Auth A on A.userId = U.userId " +
                    "WHERE A.refreshToken like :tok", Users.class)
                    .setParameter("tok", authorization.substring(7, authorization.length()))
                    .getSingleResult();

            Auth auth = em.createQuery("SELECT A FROM Auth A " +
                    "WHERE A.refreshToken like :tok", Auth.class)
                    .setParameter("tok", authorization.substring(7, authorization.length()))
                    .getSingleResult();

            String secretKey = token.generateKey(15);
            auth.setSecretKey(secretKey);
            log.info(user.getLogin());
            Timestamp timeToKill = new Timestamp(System.currentTimeMillis() + 20 * 60 * 2000);
            auth.setTimeToKillToken(timeToKill);
            String tok = token.createTok(user.getLogin(), user.getRole(), secretKey, timeToKill);
            auth.setAccessToken(tok);

            auth.setRefreshToken(token.generateKey(10));
            em.merge(auth);
            Gson gson = new Gson();
            ForAuth outAuth = new ForAuth(tok, auth.getRefreshToken());
            return Response
                    .status(Response.Status.OK)
                    .entity(outAuth)
                    .build();
//                    .entity("{ \"token\":" + g.toJson("Bearer " + tok) + "," +
//                            "\"refresh\":" + g.toJson(auth.getRefreshToken()) + "}")
//                    .build();
        } else {
            log.info("unauthorized ");
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(g.toJson("Войдите в учетную запись"))
                    .build();//выполнить
        }
    }

    @Transactional
    @GET
    @Path("/dateCheck")
    @Produces(MediaType.APPLICATION_JSON)//возвращаемый тип в формате...
    public Response dateCheck( @HeaderParam("authorization") String authorization) {
        log.info("Проверка времени жизни токена");
        Gson g = new Gson();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            java.util.Date timeToKillTok = em.createQuery("SELECT  A.timeToKillToken FROM Auth A " +
                    "WHERE A.accessToken like :tok", Date.class)
                    .setParameter("tok", authorization.substring(7, authorization.length()))
                    .getSingleResult();
            log.info(timeToKillTok.after(timestamp) + " sravnenie " + timeToKillTok);

            if (timeToKillTok.after(timestamp)) {
                return Response
                        .status(Response.Status.OK)
                        .build();//выполнить
            } else {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(g.toJson("Требуется обновить токен"))
                        .build();//выполнить
            }
        } catch (JwtException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(g.toJson("Войдите в учетную запись"))
                    .build();//выполнить
        }
    }



}
