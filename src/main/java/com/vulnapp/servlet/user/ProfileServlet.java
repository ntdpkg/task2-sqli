package com.vulnapp.servlet.user;

import com.vulnapp.model.User;
import com.vulnapp.utils.JwtUtil;
import jakarta.servlet.http.*;
import java.io.*;

public class ProfileServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // resp.sendRedirect("/profile.html");
        // return;

        resp.setContentType("application/json");
        String auth_token = JwtUtil.extractToken(req);
        if (auth_token == null || !JwtUtil.verifyToken(auth_token)) {
            resp.setStatus(401);
            resp.getWriter().println("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        try {
            User user = JwtUtil.parseToken(auth_token, User.class);
            resp.getWriter().printf("{\"success\": true, \"username\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}", user.getUsername(), user.getEmail(), user.getRole());
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(405, "POST method not allowed on /profile");
    }
}