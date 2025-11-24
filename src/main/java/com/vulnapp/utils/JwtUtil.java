package com.vulnapp.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;

import com.vulnapp.model.User;

import java.io.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class JwtUtil {
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("bruhlmaobruhlmaobruhlmaobruhlmaobruhlmaobruhlmao".getBytes());
    private static final long EXPIRATION = 86400000L; 

    private static String serializeToBase64(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
        }
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private static Object deserializeFromBase64(String base64)
            throws IOException, ClassNotFoundException {
            byte[] bytes = Base64.getDecoder().decode(base64);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }

    public static String generateToken(Object obj) {
        try {
            Instant now = Instant.now();
            return Jwts.builder()
                    .header().add("typ", "JWT").and()
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusMillis(EXPIRATION)))
                    .claim("authorized_user", serializeToBase64(obj))
                    .signWith(SECRET_KEY)
                    .compact();
        } catch (IOException e) {
            throw new RuntimeException("Serialization error: " + e.getMessage(), e);
        }
    }

    public static boolean verifyToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                            .verifyWith(SECRET_KEY)
                            .build()
                            .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            Object userDeser = claims.get("authorized_user");
            if (userDeser == null || !(userDeser instanceof String)) {
                return false;
            }
            return true;
        } catch (ExpiredJwtException eje) {
            System.err.println("Token expired: " + eje.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Token invalid: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    public static <T> T parseToken(String token, Class<T> clazz) throws IOException, ClassNotFoundException {
        Jws<Claims> jws = Jwts.parser()
                        .verifyWith(SECRET_KEY)
                        .build()
                        .parseSignedClaims(token);

        Claims claims = jws.getPayload();
        Object val = claims.get("authorized_user");
        if (val == null || !(val instanceof String)) {
            throw new IllegalArgumentException("Missing or invalid 'authorized_user' claim");
        }

        String serializedB64 = (String) val;
        Object obj = deserializeFromBase64(serializedB64);

        if (!clazz.isInstance(obj)) {
            throw new IllegalArgumentException("Deserialized object is not instance of " + clazz.getName());
        }

        return clazz.cast(obj);
    }

    public static User verifyUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user;
        try {
            user = JwtUtil.getUserFromRequest(req, User.class);
            if (user == null) {
                resp.setStatus(401);
                resp.getWriter().println("Unauthorized");
                return null;
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().println("Error: " + e.getMessage());
            return null;
        }
        return user;
    }

    public static <T> T getUserFromRequest(HttpServletRequest request, Class<T> clazz) throws IOException, ClassNotFoundException {
        String token = extractToken(request);
        if (token == null || !verifyToken(token)) {
            return null;
        }
        return parseToken(token, clazz);
    }

    public static String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if ("auth_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
