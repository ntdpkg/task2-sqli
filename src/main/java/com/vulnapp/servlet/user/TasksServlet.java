package com.vulnapp.servlet.user;

import com.vulnapp.db.Database;
import com.vulnapp.model.Task;
import com.vulnapp.model.User;
import com.vulnapp.utils.FreeMarkerConfig;
import com.vulnapp.utils.JwtUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.CallableStatement; 
import java.sql.Types;             

public class TasksServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // User user = new User(1, "test", "", "admin");
        User user = JwtUtil.verifyUser(req, resp);
        if (user == null) {
            resp.sendRedirect("/login");
            return;
        }
        String searchQuery = req.getParameter("q");
        String sortOrder = req.getParameter("order") != null ? req.getParameter("order") : "ASC";

        List<Task> tasks = new ArrayList<>();
        String sql = "{call searchTasks(?, ?, ?, ?)}";

        try (Connection conn = Database.getConnection(); CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, user.getId());
            cstmt.setString(2, searchQuery);
            cstmt.setString(3, sortOrder);
            cstmt.registerOutParameter(4, Types.REF_CURSOR);

            cstmt.execute();
            ResultSet rs = (ResultSet) cstmt.getObject(4);

            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description")));
            }
        } catch (Exception e) {
            throw new IOException("Database error", e);
        }

        try {
            Configuration cfg = FreeMarkerConfig.getConfiguration();
            Template mainTemplate = cfg.getTemplate("tasks.ftl");
            
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            
            data.put("currentQuery", searchQuery != null ? searchQuery : "");
            data.put("currentSort", sortOrder); 

            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchResultHeader = "<h2>Search Results for: " + (searchQuery != null ? searchQuery : "") + "</h2>";
                Template dynamicTemplate = new Template("dynamicSearch", new StringReader(searchResultHeader), cfg);
                StringWriter dynamicOut = new StringWriter();
                dynamicTemplate.process(data, dynamicOut);
                data.put("searchResults", dynamicOut.toString());
            }

            String taskResults;
            StringWriter dynamicOut = new StringWriter();
            if (tasks.isEmpty()) {
                taskResults = "<p>No tasks found. Add a new task to get started!</p>";
                Template dynamicTemplate = new Template("tasksEmpty", new StringReader(taskResults), cfg);
                dynamicTemplate.process(data, dynamicOut);
            } else {
                taskResults = """
                    <h3>Your Tasks:</h3>
                    <form action="/tasks" method="post">
                        <input type="hidden" name="action" value="delete">
                        <ul>
                            <#list tasks as task>
                            <li>
                                <input type="checkbox" name="task_id" value="${task.id}">
                                <b>${task.title}</b>: ${task.description}
                            </li>
                            </#list>
                        </ul>
                        <input type="submit" value="Delete Selected Tasks">
                    </form>
                    """;
                Template dynamicTemplate = new Template("taskResults", new StringReader(taskResults), cfg);
                data.put("tasks", tasks);
                dynamicTemplate.process(data, dynamicOut);
            }
            data.put("taskResults", dynamicOut.toString());

            StringWriter finalOut = new StringWriter();
            mainTemplate.process(data, finalOut);
            resp.getWriter().write(finalOut.toString());

        } catch (Exception e) {
            resp.sendError(500, "Template processing error: " + e.getMessage());
        }
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = JwtUtil.verifyUser(req, resp);
        if (user == null) {
            return;
        }
    
        String action = req.getParameter("action");
        if (action == null) {
            action = "add";
        }
    
        try (Connection conn = Database.getConnection()) {
            switch (action) {
                case "add": {
                    String title = req.getParameter("title");
                    String description = req.getParameter("description");
                    String sql = "INSERT INTO tasks (user_id, title, description) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, user.getId());
                        pstmt.setString(2, title);
                        pstmt.setString(3, description);
                        pstmt.executeUpdate();
                    }
                    break;
                }
                case "delete": {
                    String[] taskIdsToDelete = req.getParameterValues("task_id");
                    if (taskIdsToDelete != null && taskIdsToDelete.length > 0) {
                        String placeholders = String.join(",", Collections.nCopies(taskIdsToDelete.length, "?"));
                        String sql = "DELETE FROM tasks WHERE user_id = ? AND id IN (?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            pstmt.setInt(1, user.getId());
                            pstmt.setString(2, placeholders);
                            for (int i = 0; i < taskIdsToDelete.length; i++) {
                                pstmt.setInt(i + 2, Integer.parseInt(taskIdsToDelete[i]));
                            }
                            pstmt.executeUpdate();
                        }
                    }
                    break;
                }
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                    return;
            }
        } catch (Exception e) {
            resp.sendError(500, "Database error: " + e.getMessage());
            return;
        }
        resp.sendRedirect("/tasks");
    }
}