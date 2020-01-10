package ru.rsatu.seryakova.Utilites;

import io.jsonwebtoken.*;
import org.apache.log4j.Logger;
import ru.rsatu.seryakova.POJO.TokenData;

import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Random;

public class ForAuth {

    private final String secretKey = "LDyTxEhR05GJveM";

    private EntityManager em;
    private static final Logger log = Logger.getLogger(ForAuth.class);

    public String createTok(String login, String role, String secretKey) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; //алгоритм шифрования для токена

        byte[] keySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);//доп строка для шифрования - ключ для шифрования токена
        Key key = new SecretKeySpec(keySecretBytes, signatureAlgorithm.getJcaName()); //подготовили ключ для создания токена
        JwtBuilder jwtBuilder = Jwts.builder()
                .claim("login", login)
                .claim("role", role)
                .signWith(signatureAlgorithm, key);
        return jwtBuilder.compact();
    }

    public TokenData parseToken(String token, EntityManager em) {
        if (token != null) {
            try{
                String secretKey = em.createQuery("SELECT  A.secretKey FROM Auth A " +
                        "WHERE A.accessToken like :tok", String.class)
                        .setParameter("tok", token.substring(7, token.length()))
                        .getSingleResult();

                Claims claims = Jwts.parser()
                        .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                        .parseClaimsJws(token.substring(7))
                        .getBody();
                return new TokenData(claims.get("login", String.class), claims.get("role", String.class));

            } catch (JwtException e) {
                log.info("Неверный токен");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            log.info("Пустой токен");
            return null;
        }
    }


    public String generateKey(int lenght) {
        String characters = new String("1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm");
        Random rng = new Random();
        char[] text = new char[lenght];
        for (int i = 0; i < lenght; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
}
