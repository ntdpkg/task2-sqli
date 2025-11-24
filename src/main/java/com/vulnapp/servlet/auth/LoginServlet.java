package com.vulnapp.servlet.auth;

import com.vulnapp.db.Database;
import com.vulnapp.model.User;
import com.vulnapp.utils.JwtUtil;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (JwtUtil.verifyUser(req, resp) != null) {
            resp.sendRedirect("/tasks");
            return;
        }
        resp.sendRedirect("/login.html");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, username, email, role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role")
                );

                String token = JwtUtil.generateToken(user);

                resp.getWriter().printf("{\"success\": true, \"token\": \"%s\"}", token);
            } else {
                resp.setStatus(401);
                resp.getWriter().println("{\"success\": false, \"message\": \"Invalid credentials\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}