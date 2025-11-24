package com.vulnapp.servlet.auth;

import com.vulnapp.db.Database;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.PreparedStatement;

public class RegisterServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect("/register.html");
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");

        try (var conn = Database.getConnection()) {
            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            resp.getWriter().printf("User %s registered successfully. Please login now.", username);            
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().printf("Error: %s", e.getMessage());
        }
    }
}