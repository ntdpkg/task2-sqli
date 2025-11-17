package com.vulnapp;

import com.vulnapp.db.Database;
import com.vulnapp.servlet.*;
import com.vulnapp.servlet.auth.LoginServlet;
import com.vulnapp.servlet.auth.RegisterServlet;
import com.vulnapp.servlet.user.ImportTasksServlet;
import com.vulnapp.servlet.user.ProfileServlet;
import com.vulnapp.servlet.user.TasksServlet;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.catalina.servlets.DefaultServlet;

@WebListener
public class Main implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Database.init();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database!");
            e.printStackTrace();
            return;
        }

        ServletContext ctx = sce.getServletContext();

        // Đăng ký các servlets giống như trước đây, nhưng sử dụng ServletContext
        ctx.addServlet("Default", new DefaultServlet()).addMapping("/");
        ctx.addServlet("Welcome", new WelcomeServlet()).addMapping("");

        ctx.addServlet("Register", new RegisterServlet()).addMapping("/register");
        ctx.addServlet("Login", new LoginServlet()).addMapping("/login");
        ctx.addServlet("Profile", new ProfileServlet()).addMapping("/profile");
        ctx.addServlet("Tasks", new TasksServlet()).addMapping("/tasks");

        var wrapper = ctx.addServlet("ImportTasks", new ImportTasksServlet());
        wrapper.setMultipartConfig(new MultipartConfigElement(
                "",
                10 * 1024 * 1024,   // 10MB
                50 * 1024 * 1024,   // 50MB
                1 * 1024 * 1024     // 1MB
        ));
        wrapper.addMapping("/importTasks");

        System.out.println("Application servlets configured and started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Có thể thêm logic dọn dẹp ở đây nếu cần
        System.out.println("Application shutting down.");
    }
}